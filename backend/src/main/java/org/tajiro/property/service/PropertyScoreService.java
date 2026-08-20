package org.tajiro.property.service;

import org.tajiro.property.domain.PropertyVO;
import org.tajiro.property.domain.PropertyValueAnalysisResultVO;
import org.tajiro.property.dto.PropertyComparisonScoreDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface PropertyScoreService {
    Map<Long, PropertyValueAnalysisResultVO> saveScores(Long userId, List<PropertyVO> properties);

    Map<Long, PropertyComparisonScoreDTO> calculateComparisonScores(
            Long userId,
            List<Long> propertyIds,
            BigDecimal workplaceLatitude,
            BigDecimal workplaceLongitude);

    Map<Long, PropertyValueAnalysisResultVO> recalculateAllScores(Long userId);
}
