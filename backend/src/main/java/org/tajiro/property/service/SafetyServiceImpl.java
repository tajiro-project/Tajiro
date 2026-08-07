package org.tajiro.property.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.tajiro.common.api.ErrorCode;
import org.tajiro.exception.BusinessException;
import org.tajiro.property.dto.PropertySafetyDTO;
import org.tajiro.property.mapper.SafetyMapper;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class SafetyServiceImpl implements SafetyService {

    private final SafetyMapper safetyMapper;

    // 범죄안전 카테고리 5개 정의
    private static final Set<String> CRIME_SAFETY_CATEGORIES = Set.of(
            "POLICE_CENTER",       // 경찰서·지구대
            "CCTV",                // CCTV
            "SAFETY_BELL",         // 안전 비상벨
            "SECURITY_LIGHT",      // 보안등
            "CHILD_GUARD_HOUSE"    // 아동안전지킴이집
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

        // 범죄안전 5개 카테고리 개수 계산
        int crimeCount = (int) result.getSafetyList().stream()
                .filter(item -> item.getSafeCategory() != null && CRIME_SAFETY_CATEGORIES.contains(item.getSafeCategory()))
                .count();

        // 나머지는 교통안전 카테고리 개수로 계산
        int trafficCount = result.getSafetyList().size() - crimeCount;

        result.setCrimeSafetyCount(crimeCount);
        result.setTrafficSafetyCount(trafficCount);

        return result;
    }
}