package org.tajiro.comparison.service;

import org.tajiro.comparison.dto.ComparePropertyDTO;
import org.tajiro.comparison.dto.ComparisonMetricsResponseDTO;

import java.util.List;

public interface ComparisonService {

    List<ComparePropertyDTO> getCompareProperties(Long userId);

    void addCompareProperty(Long userId, Long propertyId);

    void removeCompareProperty(Long userId, Long propertyId);

    ComparisonMetricsResponseDTO getComparisonMetrics(Long userId, List<Long> propertyIds);
}
