package org.tajiro.comparison.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComparisonAnalysisResponseDTO {
    private Long reportId;
    private String aiPropertySummaryText;
    private String aiSummary;
    private String aiSafetySummary;
    private Long aiRecommendedPropertyId;
    private String aiAtp;
}
