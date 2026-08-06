package org.tajiro.property.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.tajiro.property.domain.PropertyVO;
import org.tajiro.property.domain.PropertyValueAnalysisResultVO;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@Log4j2
public class StubPropertyScoreService implements PropertyScoreService{

    @Override
    public Map<Long, PropertyValueAnalysisResultVO> saveScores(Long userId, List<PropertyVO> properties) {
        log.info("[Stub] saveScores userId=" + userId + " 대상=" + properties.size() + "건");
        return Collections.emptyMap();
    }
}
