package org.tajiro.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComparisonReportResponse {
    private Long reportId;
    private String title;
    private List<Long> comparedPropertyIds;
    private List<ComparisonReportPropertyDTO> comparedProperties;
    private String aiPropertySummaryText;
    private String aiSummary;
    private Long aiRecommendedPropertyId;
    private String aiAtp;
    private LocalDateTime createdAt;
}
