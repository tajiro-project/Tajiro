package org.tajiro.comparison.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComparisonMetricsResponseDTO {
    //ComparisonMetricDTO를 items로 묶어서 반환하는 DTO
    private List<ComparisonMetricDTO> items;
}
