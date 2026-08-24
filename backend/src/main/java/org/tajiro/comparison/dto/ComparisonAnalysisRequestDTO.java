package org.tajiro.comparison.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComparisonAnalysisRequestDTO {
    private List<Long> propertyIds;
}
