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
import org.tajiro.property.domain.PropertyValueAnalysisResultVO;
import org.tajiro.property.dto.PropertyListDTO;
import org.tajiro.property.mapper.PropertyMapper;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PropertyServiceImpl implements PropertyService{
    private static final int REGION_RADIUS_METERS = 1500;

    private final PropertyMapper propertyMapper;
    private final PreferenceMapper preferenceMapper;
    private final PropertyScoreService propertyScoreService;

    @Override
    public List<PropertyListDTO> getList(Long userId, BigDecimal centerLat, BigDecimal centerLng) {
        List<PropertyVO> properties;

        if(centerLat != null && centerLng != null) {
            properties = findByRegion(centerLat, centerLng);
        }
        else {
            properties = findMatchingProperties(userId);
            fillMissingScores(userId, properties);
        }

        return properties.stream()
                .map(PropertyListDTO::of)
                .collect(Collectors.toList());
    }

    private List<PropertyVO> findByRegion(BigDecimal centerLat, BigDecimal centerLng) {
        return propertyMapper.getList(PropertySearchRequest.builder()
                .refLat(centerLat)
                .refLng(centerLng)
                .radiusMeters(REGION_RADIUS_METERS)
                .build());
    }

    private void fillMissingScores(Long userId, List<PropertyVO> properties) {
        List<PropertyVO> missing = properties.stream()
                .filter(p->p.getPropertyValueAnalysisResultVO() == null)
                .collect(Collectors.toList());

        if(missing.isEmpty()) {
            return;
        }

        Map<Long, PropertyValueAnalysisResultVO> scores = propertyScoreService.saveScores(userId, missing);

        for(PropertyVO property : missing) {
            PropertyValueAnalysisResultVO score = scores.get(property.getId());
            if(score != null) {
                property.setPropertyValueAnalysisResultVO(score);
            }
        }
    }

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
        List<String> tradeTypes = split(preferenceVO.getTradeTypes());
        boolean searchAllTradeTypes = tradeTypes == null;

        return PropertySearchRequest.builder()
                .userId(userId)
                .refLat(preferenceVO.getWorkplaceLatitude())
                .refLng(preferenceVO.getWorkplaceLongitude())
                .maxWorkplaceDistanceMeters(preferenceVO.getMaxWorkplaceDistanceMeters())
                .propertyTypes(split(preferenceVO.getHousingTypes()))
                .tradeTypes(tradeTypes)
                .floorPreference(split(preferenceVO.getFloorPreference()))
                .minDeposit(searchAllTradeTypes ? null : preferenceVO.getMinDeposit())
                .maxDeposit(searchAllTradeTypes ? null : preferenceVO.getMaxDeposit())
                .minMonthlyRent(searchAllTradeTypes ? null : preferenceVO.getMinMonthlyRent())
                .maxMonthlyRent(searchAllTradeTypes ? null : preferenceVO.getMaxMonthlyRent())
                .minSellingPrice(searchAllTradeTypes ? null : preferenceVO.getMinSellingPrice())
                .maxSellingPrice(searchAllTradeTypes ? null : preferenceVO.getMaxSellingPrice())
                .minAreaM2(preferenceVO.getMinArea())
                .maxAreaM2(preferenceVO.getMaxArea())
                .desiredInfraCategories(preferenceVO.getDesiredInfraCategories())
                .desiredAmenityCategories(preferenceVO.getDesiredAmenityCategories())
                .applyDesiredCategoryFilter(true)
                .useAllCategoriesWhenEmpty(true)
                .hasCar(preferenceVO.getHasCar())
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
