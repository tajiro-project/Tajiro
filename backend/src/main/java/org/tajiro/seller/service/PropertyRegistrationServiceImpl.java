package org.tajiro.seller.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tajiro.common.api.ErrorCode;
import org.tajiro.exception.BusinessException;
import org.tajiro.seller.domain.PropertyRegistrationVO;
import org.tajiro.seller.dto.PropertyRegistrationRequest;
import org.tajiro.seller.dto.PropertyRegistrationResponse;
import org.tajiro.seller.event.PropertyRegisteredEvent;
import org.tajiro.seller.mapper.PropertyRegistrationMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Log4j2
public class PropertyRegistrationServiceImpl implements PropertyRegistrationService {

    private static final Set<String> PROPERTY_TYPES = Set.of(
            "원룸", "아파트", "주택/빌라", "오피스텔"
    );
    private static final Set<String> TRADE_TYPES = Set.of("월세", "전세", "매매");
    private static final Pattern FLOOR_INFO_PATTERN = Pattern.compile(
            "^(?:[1-9]\\d*|지하[1-9]\\d*|반지하|옥탑) / [1-9]\\d*층$"
    );
    private static final Pattern HTTP_URL_PATTERN = Pattern.compile("^https?://.+");
    private static final int MAX_IMAGE_COUNT = 10;

    private final PropertyRegistrationMapper propertyRegistrationMapper;
    private final ObjectProvider<BuildingResolveService> buildingResolveServiceProvider;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public PropertyRegistrationResponse register(
            Long sellerId,
            PropertyRegistrationRequest request
    ) {
        if (sellerId == null) {
            throw new BusinessException(ErrorCode.AUTH_REQUIRED);
        }

        validate(request);

        String propertyType = normalizePropertyType(request.getPropertyType());
        List<String> imageUrls = normalizeImageUrls(request.getImageUrls());
        Long buildingId = resolveBuilding(request);

        PropertyRegistrationVO property = PropertyRegistrationVO.builder()
                .sellerId(sellerId)
                .buildingId(buildingId)
                .title(defaultTitle(request.getTitle(), propertyType))
                .propertyType(propertyType)
                .tradeType(request.getTradeType())
                .deposit(request.getDeposit())
                .monthlyRent(monthlyRentOf(request))
                .maintenanceFee(defaultNonNegative(request.getMaintenanceFee()))
                .areaM2(request.getAreaM2())
                .floorInfo(request.getFloorInfo().trim())
                .roomNumber(normalizeRoomNumber(request.getRoomNumber()))
                .address(request.getLocation().getAddress().trim())
                .roomNum(defaultPositive(request.getRoomNum()))
                .bathroomNum(defaultPositive(request.getBathroomNum()))
                .parkAvailability(Boolean.TRUE.equals(request.getParkAvailability()))
                .transactionStatus(true)
                .propertyDescription(trimToNull(request.getPropertyDescription()))
                .availableDate(request.getAvailableDate())
                .moveInDate(request.getMoveInDate())
                .updateDate(LocalDateTime.now())
                .discussionStatus(Boolean.TRUE.equals(request.getDiscussionStatus()))
                .dong(trimToNull(request.getDong()))
                .build();

        int inserted = propertyRegistrationMapper.insertProperty(property);
        if (inserted != 1 || property.getId() == null) {
            throw new IllegalStateException("매물 등록 결과를 확인할 수 없습니다.");
        }

        if (!imageUrls.isEmpty()) {
            int insertedImages = propertyRegistrationMapper.insertPropertyImages(
                    property.getId(), imageUrls
            );
            if (insertedImages != imageUrls.size()) {
                throw new IllegalStateException("매물 이미지가 모두 저장되지 않았습니다.");
            }
        }

        eventPublisher.publishEvent(
                new PropertyRegisteredEvent(property.getId(), buildingId)
        );

        boolean resolved = buildingId != null;
        return PropertyRegistrationResponse.builder()
                .propertyId(property.getId())
                .buildingId(buildingId)
                .locationStatus(resolved ? "RESOLVED" : "PENDING")
                .aggregationStatus(resolved ? "PENDING" : "WAITING_FOR_LOCATION")
                .build();
    }

    private void validate(PropertyRegistrationRequest request) {
        if (request == null
                || isBlank(request.getPropertyType())
                || !PROPERTY_TYPES.contains(normalizePropertyType(request.getPropertyType()))
                || !TRADE_TYPES.contains(request.getTradeType())
                || request.getDeposit() == null
                || request.getDeposit() < 0
                || request.getAreaM2() == null
                || request.getAreaM2().compareTo(BigDecimal.ZERO) <= 0
                || isBlank(request.getFloorInfo())
                || !FLOOR_INFO_PATTERN.matcher(request.getFloorInfo().trim()).matches()
                || request.getAvailableDate() == null
                || request.getLocation() == null
                || isBlank(request.getLocation().getAddress())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        if ("월세".equals(request.getTradeType())
                && (request.getMonthlyRent() == null || request.getMonthlyRent() < 0)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (request.getMaintenanceFee() != null && request.getMaintenanceFee() < 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (request.getRoomNum() != null && request.getRoomNum() < 1) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (request.getBathroomNum() != null && request.getBathroomNum() < 1) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        validateCoordinates(request.getLocation());
        normalizeImageUrls(request.getImageUrls());
    }

    private void validateCoordinates(PropertyRegistrationRequest.LocationRequest location) {
        BigDecimal latitude = location.getLat();
        BigDecimal longitude = location.getLng();
        if ((latitude == null) != (longitude == null)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (latitude == null) {
            return;
        }
        if (latitude.compareTo(new BigDecimal("33")) < 0
                || latitude.compareTo(new BigDecimal("39")) > 0
                || longitude.compareTo(new BigDecimal("124")) < 0
                || longitude.compareTo(new BigDecimal("132")) > 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private Long resolveBuilding(PropertyRegistrationRequest request) {
        BuildingResolveService resolver = buildingResolveServiceProvider.getIfAvailable();
        if (resolver == null) {
            return null;
        }

        PropertyRegistrationRequest.LocationRequest location = request.getLocation();
        try {
            return resolver.resolveBuildingId(
                    location.getAddress().trim(),
                    trimToNull(location.getBuildingName()),
                    trimToNull(request.getDong()),
                    location.getLat(),
                    location.getLng()
            );
        } catch (RuntimeException exception) {
            log.warn("건물 위치 해석 실패 - 매물을 위치 확인 대기 상태로 등록합니다.", exception);
            return null;
        }
    }

    private List<String> normalizeImageUrls(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return Collections.emptyList();
        }
        if (imageUrls.size() > MAX_IMAGE_COUNT) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        List<String> normalized = new ArrayList<>(imageUrls.size());
        for (String imageUrl : imageUrls) {
            String value = trimToNull(imageUrl);
            if (value == null || value.length() > 2048
                    || !HTTP_URL_PATTERN.matcher(value).matches()) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
            }
            normalized.add(value);
        }
        return normalized;
    }

    private String normalizePropertyType(String propertyType) {
        if (propertyType == null) {
            return null;
        }
        String normalized = propertyType.trim();
        return "빌라/주택".equals(normalized) ? "주택/빌라" : normalized;
    }

    private String defaultTitle(String title, String propertyType) {
        String normalized = trimToNull(title);
        return normalized == null ? propertyType : normalized;
    }

    private int monthlyRentOf(PropertyRegistrationRequest request) {
        return "월세".equals(request.getTradeType()) ? request.getMonthlyRent() : 0;
    }

    private int defaultNonNegative(Integer value) {
        return value == null ? 0 : value;
    }

    private int defaultPositive(Integer value) {
        return value == null ? 1 : value;
    }

    private String normalizeRoomNumber(String roomNumber) {
        String value = trimToNull(roomNumber);
        if (value == null || value.endsWith("호")) {
            return value;
        }
        return value + "호";
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
