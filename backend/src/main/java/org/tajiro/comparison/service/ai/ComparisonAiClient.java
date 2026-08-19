package org.tajiro.comparison.service.ai;

import org.tajiro.comparison.dto.ComparisonAnalysisResponseDTO;
import org.tajiro.comparison.dto.ComparisonMetricDTO;
import org.tajiro.preference.domain.HousingPreferenceVO;

import java.util.List;

public interface ComparisonAiClient {
    ComparisonAnalysisResponseDTO generate(
            List<ComparisonMetricDTO> properties,
            List<String> priorities,
            HousingPreferenceVO preference);
}
