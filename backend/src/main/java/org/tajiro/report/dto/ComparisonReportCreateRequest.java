package org.tajiro.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComparisonReportCreateRequest {
    private String title;
    private List<Long> comparedPropertyIds;
    private String aiPropertySummaryText;
    private String aiSummary;
    private Long aiRecommendedPropertyId;
    private String aiAtp;
}
