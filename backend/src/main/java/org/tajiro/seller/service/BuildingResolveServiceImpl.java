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

@Slf4j
@Service
@RequiredArgsConstructor
public class BuildingResolveServiceImpl implements BuildingResolveService {
    private static final double COORD_TOLERANCE_METERS = 100;

    private static final int NEARBY_RADIUS_METERS = 30;

    private static final double EARTH_RADIUS_METERS = 6_371_000;

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

        BuildingCreateVO building = BuildingCreateVO.builder()
                .buildingCode(buildingCode)
                .roadAddress(roadAddress)
                .jibunAddress(address.getJibunAddress())
                .bldNm(trimToNull(picked.getBldNm()))
                .dongNm(trimToNull(picked.getDongNm()))
                .pnu(address.getPnu())
                .latitude(address.getLat())
                .longitude(address.getLng())
                .build();

        buildingMapper.insertBuilding(building);
        log.info("[건물해석] 신규 건물 등록 code={} id={}", buildingCode, building.getId());

        return building.getId();
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
}