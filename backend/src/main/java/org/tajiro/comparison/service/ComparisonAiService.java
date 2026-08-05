package org.tajiro.comparison.service;

import org.tajiro.comparison.dto.ComparisonAnalysisRequestDTO;
import org.tajiro.comparison.dto.ComparisonAnalysisResponseDTO;

public interface ComparisonAiService {
    ComparisonAnalysisResponseDTO analyze(Long userId, ComparisonAnalysisRequestDTO request);
}
