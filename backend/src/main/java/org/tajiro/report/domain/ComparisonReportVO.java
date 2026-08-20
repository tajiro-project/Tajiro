package org.tajiro.report.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComparisonReportVO {
    private Long reportId;
    private Long userId;
    private Long aiRecommendedPropertyId;
    private String title;
    private String comparedPropertyIdsJson;
    private String preferencePrioritiesJson;
    private String aiPropertySummaryText;
    private String aiSummary;
    private String aiSafetySummary;
    private Boolean saved;
    private String aiAtp;
    private LocalDateTime createdAt;
}
