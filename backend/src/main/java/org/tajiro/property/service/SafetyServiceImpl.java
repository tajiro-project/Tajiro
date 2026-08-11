package org.tajiro.property.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.tajiro.common.api.ErrorCode;
import org.tajiro.exception.BusinessException;
import org.tajiro.property.dto.PropertySafetyDTO;
import org.tajiro.property.mapper.SafetyMapper;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SafetyServiceImpl implements SafetyService {

    private final SafetyMapper safetyMapper;

    private static final Set<String> CRIME_SAFETY_CATEGORIES = Set.of(
            "POLICE_CENTER", "CCTV", "SAFETY_BELL", "SECURITY_LIGHT", "CHILD_GUARD_HOUSE"
    );

    @Override
    public PropertySafetyDTO getPropertySafetyInfo(Long propertyId) {
        if (!safetyMapper.existsPropertyById(propertyId)) {
            throw new BusinessException(ErrorCode.PROPERTY_NOT_FOUND);
        }

        PropertySafetyDTO result = safetyMapper.selectPropertySafetyInfo(propertyId);

        if (result == null || result.getSafetyList() == null || result.getSafetyList().isEmpty()) {
            throw new BusinessException(ErrorCode.INFRASTRUCTURE_NOT_FOUND);
        }

        // 1. 전체 안전 정보 중 가장 최신 업데이트 일시 추출하여 최상위에 세팅
        LocalDateTime latestUpdate = result.getSafetyList().stream()
                .map(PropertySafetyDTO.SafetyItemDTO::getUpdatedAt)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        result.setUpdatedAt(latestUpdate);

        // 2. 범죄/교통 안전 카테고리 수 집계
        int crimeCount = (int) result.getSafetyList().stream()
                .filter(item -> item.getSafeCategory() != null && CRIME_SAFETY_CATEGORIES.contains(item.getSafeCategory()))
                .count();

        int trafficCount = result.getSafetyList().size() - crimeCount;

        result.setCrimeSafetyCount(crimeCount);
        result.setTrafficSafetyCount(trafficCount);

        return result;
    }
}