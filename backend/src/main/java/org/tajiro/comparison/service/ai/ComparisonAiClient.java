package org.tajiro.comparison.service.ai;

import org.tajiro.comparison.dto.ComparisonAnalysisResponseDTO;
import org.tajiro.comparison.dto.ComparisonMetricDTO;

import java.util.List;

public interface ComparisonAiClient {
    ComparisonAnalysisResponseDTO generate(
            List<ComparisonMetricDTO> properties,
            List<String> priorities);
}
