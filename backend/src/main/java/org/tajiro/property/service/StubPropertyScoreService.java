package org.tajiro.property.service;

import org.springframework.stereotype.Service;
import org.tajiro.property.domain.PropertyVO;
import org.tajiro.property.domain.PropertyValueAnalysisResultVO;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class StubPropertyScoreService implements PropertyScoreService{

    @Override
    public Map<Long, PropertyValueAnalysisResultVO> saveScores(Long userId, List<PropertyVO> properties) {
        return Collections.emptyMap();
    }
}
