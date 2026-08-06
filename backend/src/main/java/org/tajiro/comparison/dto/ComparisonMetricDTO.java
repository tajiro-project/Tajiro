package org.tajiro.comparison.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComparisonMetricDTO {

    private Long propertyId;
    private String title;
    private String propertyType;
    private String tradeType;
    private Integer deposit;
    private Integer monthlyRent;
    private Integer maintenanceFee;
    private BigDecimal areaM2;
    private String floorInfo;
    private LocalDateTime updateDate;
    private Integer commuteMinutes;
    private Integer infraCount;
    private Integer evaluationScore;
    private Integer cctvCountWithin500m;
    private Integer policeCountWithin500m;
    private Integer childrenCountWithin500m;
    private Integer bellCountWithin500m;
    private Integer safetyLightCountWithin500m;
}
