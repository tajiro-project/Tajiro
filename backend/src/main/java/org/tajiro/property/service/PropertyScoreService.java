package org.tajiro.property.service;

import org.tajiro.property.domain.PropertyVO;
import org.tajiro.property.domain.PropertyValueAnalysisResultVO;

import java.util.List;
import java.util.Map;

public interface PropertyScoreService {
    Map<Long, PropertyValueAnalysisResultVO> saveScores(Long userId, List<PropertyVO> properties);

    Map<Long, PropertyValueAnalysisResultVO> recalculateAllScores(Long userId);
}
