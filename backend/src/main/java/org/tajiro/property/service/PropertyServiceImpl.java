package org.tajiro.property.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tajiro.common.api.ErrorCode;
import org.tajiro.exception.BusinessException;
import org.tajiro.preference.domain.HousingPreferenceVO;
import org.tajiro.preference.dto.PropertySearchRequest;
import org.tajiro.preference.mapper.PreferenceMapper;
import org.tajiro.property.domain.PropertyVO;
import org.tajiro.property.mapper.PropertyMapper;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PropertyServiceImpl implements PropertyService{
    private final PropertyMapper propertyMapper;
    private final PreferenceMapper preferenceMapper;

    @Override
    @Transactional(readOnly = true)
    public List<PropertyVO> findMatchingProperties(Long userId) {
        HousingPreferenceVO preferenceVO = preferenceMapper.findByUserId(userId);

        if(preferenceVO == null) {
            throw new BusinessException(ErrorCode.PREFERENCE_NOT_FOUND);
        }
        return propertyMapper.getList(toSearchRequest(userId, preferenceVO));
    }

    private PropertySearchRequest toSearchRequest(Long userId, HousingPreferenceVO preferenceVO) {
        return PropertySearchRequest.builder()
                .userId(userId)
                .refLat(preferenceVO.getWorkplaceLatitude())
                .refLng(preferenceVO.getWorkplaceLongitude())
                .maxWorkplaceDistanceMeters(preferenceVO.getMaxWorkplaceDistanceMeters())
                .propertyTypes(split(preferenceVO.getHousingTypes()))
                .tradeTypes(split(preferenceVO.getTradeTypes()))
                .floorPreference(split(preferenceVO.getFloorPreference()))
                .minDeposit(preferenceVO.getMinDeposit())
                .maxDeposit(preferenceVO.getMaxDeposit())
                .minMonthlyRent(preferenceVO.getMinMonthlyRent())
                .maxMonthlyRent(preferenceVO.getMaxMonthlyRent())
                .minSellingPrice(preferenceVO.getMinSellingPrice())
                .maxSellingPrice(preferenceVO.getMaxSellingPrice())
                .minAreaM2(preferenceVO.getMinArea())
                .maxAreaM2(preferenceVO.getMaxArea())
                .desiredInfraCategories(preferenceVO.getDesiredInfraCategories())
                .desiredAmenityCategories(preferenceVO.getDesiredAmenityCategories())
                .build();
    }

    private List<String> split(String csv) {
        if (csv == null || csv.trim().isEmpty()) {
            return null;
        }
        List<String> parts = Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .collect(Collectors.toList());
        return parts.isEmpty() ? null : parts;
    }
}
