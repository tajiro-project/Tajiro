package org.tajiro.seller.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tajiro.location.dto.AddressResolveResult;
import org.tajiro.location.dto.BuildingLedgerItem;
import org.tajiro.location.service.BuildingLedgerClient;
import org.tajiro.location.service.KakaoLocalClient;
import org.tajiro.seller.domain.BuildingCreateVO;
import org.tajiro.seller.mapper.BuildingMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class BuildingResolveServiceImpl implements BuildingResolveService {
    private static final double COORD_TOLERANCE_METERS = 100;

    private static final int NEARBY_RADIUS_METERS = 30;

    /** 동 좌표 후보로 인정할 지번 좌표로부터의 최대 거리 */
    private static final int DONG_MATCH_RADIUS_METERS = 500;

    private static final double EARTH_RADIUS_METERS = 6_371_000;

    /** 장소명에서 "105동" 같은 동 표기를 찾는다 */
    private static final Pattern PLACE_DONG = Pattern.compile("(\\d+)\\s*동");

    /** 지번 주소 끝의 번지("822-1", "1114")를 판별한다 */
    private static final Pattern JIBUN_NUMBER = Pattern.compile("^\\d+(-\\d+)?$");

    private final KakaoLocalClient kakaoLocalClient;
    private final BuildingLedgerClient buildingLedgerClient;
    private final BuildingMapper buildingMapper;

    @Override
    @Transactional
    public Long resolveBuildingId(
            String roadAddress,
            String buildingName,
            String dongName,
            BigDecimal latitude,
            BigDecimal longitude) {

        AddressResolveResult address = geocode(roadAddress, latitude, longitude);

        if (address == null) {
            log.warn("[건물해석] 지오코딩 실패 address={}", roadAddress);
            return findNearby(latitude, longitude);
        }

        List<BuildingLedgerItem> items = buildingLedgerClient.fetchTitleItems(address);
        BuildingLedgerItem picked = buildingLedgerClient.pick(items, dongName, buildingName);

        if (picked == null || isBlank(picked.getMgmBldrgstPk())) {
            log.warn("[건물해석] 건물번호 없음 address={} 표제부={}건", roadAddress, items.size());
            return findNearby(address.getLat(), address.getLng());
        }

        String buildingCode = picked.getMgmBldrgstPk().trim();

        Long existing = buildingMapper.findIdByBuildingCode(buildingCode);
        if (existing != null) {
            log.info("[건물해석] 기존 건물 재사용 code={} id={}", buildingCode, existing);
            return existing;
        }

        // 지번 대표 좌표를 그대로 쓰면 같은 단지의 동들이 모두 같은 위치가 된다.
        // 카카오 장소 검색으로 동별 좌표를 찾고, 없으면 지번 좌표로 폴백한다.
        BigDecimal[] point = resolveDongPoint(
                address,
                firstNonBlank(picked.getBldNm(), buildingName),
                firstNonBlank(dongName, picked.getDongNm()));

        BuildingCreateVO building = BuildingCreateVO.builder()
                .buildingCode(buildingCode)
                .roadAddress(roadAddress)
                .jibunAddress(address.getJibunAddress())
                .bldNm(trimToNull(picked.getBldNm()))
                .dongNm(trimToNull(picked.getDongNm()))
                .pnu(address.getPnu())
                .latitude(point == null ? address.getLat() : point[0])
                .longitude(point == null ? address.getLng() : point[1])
                .build();

        buildingMapper.insertBuilding(building);
        log.info("[건물해석] 신규 건물 등록 code={} id={} 동좌표={}",
                buildingCode, building.getId(), point != null);

        return building.getId();
    }

    /**
     * 아파트 동별 좌표를 카카오 장소 검색으로 해석한다.
     * 지번 좌표 기준 반경 안에서, 장소명에 같은 동 번호가 있는 것 중 가장 가까운 곳을 고른다.
     *
     * @return 좌표 2개(위도, 경도). 찾지 못하면 null 이며 호출부가 지번 좌표로 폴백한다.
     */
    private BigDecimal[] resolveDongPoint(
            AddressResolveResult address, String buildingName, String dongName) {

        String dongKey = digitsOf(dongName);
        if (dongKey.isEmpty() || address.getLat() == null || address.getLng() == null) {
            return null;
        }

        String query = buildDongQuery(address, buildingName, dongKey);

        List<AddressResolveResult> places;
        try {
            places = kakaoLocalClient.searchPlaces(query);
        } catch (RuntimeException e) {
            // 동 좌표를 못 찾는다고 매물 등록까지 실패시키지 않는다
            log.warn("[건물해석] 동 좌표 검색 실패 query={}", query);
            return null;
        }

        AddressResolveResult nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (AddressResolveResult place : places) {
            if (place.getLat() == null || place.getLng() == null) {
                continue;
            }
            if (!matchesDong(place.getBuildingName(), dongKey)) {
                continue;
            }

            double distance = distanceMeters(
                    address.getLat(), address.getLng(), place.getLat(), place.getLng());
            if (distance > DONG_MATCH_RADIUS_METERS) {
                continue;
            }
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = place;
            }
        }

        if (nearest == null) {
            log.info("[건물해석] 동 좌표 미발견, 지번 좌표 사용 query={}", query);
            return null;
        }

        log.info("[건물해석] 동 좌표 적용 query={} 장소={} 거리={}m",
                query, nearest.getBuildingName(), (int) nearestDistance);
        return new BigDecimal[]{nearest.getLat(), nearest.getLng()};
    }

    /** "안중읍 현화리 화현마을우림필유 105동" 형태의 검색어를 만든다 */
    private String buildDongQuery(
            AddressResolveResult address, String buildingName, String dongKey) {

        StringBuilder query = new StringBuilder();

        String region = regionOf(address.getJibunAddress());
        if (hasText(region)) {
            query.append(region).append(' ');
        }
        if (hasText(buildingName)) {
            query.append(buildingName.trim()).append(' ');
        }
        return query.append(dongKey).append('동').toString();
    }

    /** 지번 주소에서 끝의 번지를 떼어 행정구역만 남긴다 */
    private String regionOf(String jibunAddress) {
        if (!hasText(jibunAddress)) {
            return null;
        }
        String[] tokens = jibunAddress.trim().split("\\s+");
        int end = tokens.length;
        if (end > 0 && JIBUN_NUMBER.matcher(tokens[end - 1]).matches()) {
            end--;
        }
        return String.join(" ", java.util.Arrays.copyOfRange(tokens, 0, end));
    }

    /** 장소명에 해당 동 번호가 들어있는지 확인한다 */
    private boolean matchesDong(String placeName, String dongKey) {
        if (!hasText(placeName)) {
            return false;
        }
        Matcher matcher = PLACE_DONG.matcher(placeName);
        while (matcher.find()) {
            if (sameNumber(matcher.group(1), dongKey)) {
                return true;
            }
        }
        return false;
    }

    private boolean sameNumber(String a, String b) {
        try {
            return Long.parseLong(a) == Long.parseLong(b);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private String digitsOf(String value) {
        return value == null ? "" : value.replaceAll("\\D", "");
    }

    private String firstNonBlank(String first, String second) {
        return hasText(first) ? first : second;
    }

    private AddressResolveResult geocode(
            String roadAddress, BigDecimal latitude, BigDecimal longitude) {

        List<AddressResolveResult> results;
        try {
            results = kakaoLocalClient.searchAddress(roadAddress);
        } catch (RuntimeException e) {
            log.warn("[건물해석] 주소 검색 실패 address={}", roadAddress, e);
            return null;
        }

        if (results.isEmpty()) {
            return null;
        }
        if (latitude == null || longitude == null) {
            return results.get(0);
        }

        AddressResolveResult nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (AddressResolveResult candidate : results) {
            if (candidate.getLat() == null || candidate.getLng() == null) {
                continue;
            }
            double distance = distanceMeters(
                    latitude, longitude, candidate.getLat(), candidate.getLng());
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = candidate;
            }
        }

        if (nearest == null) {
            return results.get(0);
        }
        if (nearestDistance > COORD_TOLERANCE_METERS) {
            log.warn("[건물해석] 좌표 불일치 {}m address={}", (int) nearestDistance, roadAddress);
        }
        return nearest;
    }

    private Long findNearby(BigDecimal latitude, BigDecimal longitude) {
        if (latitude == null || longitude == null) {
            return null;
        }

        Long nearby = buildingMapper.findNearbyId(latitude, longitude, NEARBY_RADIUS_METERS);
        if (nearby != null) {
            log.info("[건물해석] 좌표로 기존 건물 재사용 id={}", nearby);
        }
        return nearby;
    }

    private double distanceMeters(
            BigDecimal lat1, BigDecimal lng1, BigDecimal lat2, BigDecimal lng2) {

        double radLat1 = Math.toRadians(lat1.doubleValue());
        double radLat2 = Math.toRadians(lat2.doubleValue());
        double deltaLat = radLat2 - radLat1;
        double deltaLng = Math.toRadians(lng2.doubleValue() - lng1.doubleValue());

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(radLat1) * Math.cos(radLat2)
                * Math.sin(deltaLng / 2) * Math.sin(deltaLng / 2);

        return EARTH_RADIUS_METERS * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}