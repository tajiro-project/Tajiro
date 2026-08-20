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
    private Boolean hasCar;
    private Integer commuteMinutes;
    private Integer preferenceScore;
    private Integer commuteScore;
    private Integer costScore;
    private Integer infraScore;
    private Integer amenityScore;
    private Integer infraCount;
    private Integer amenityCount;
    private BigDecimal evaluationScore;
    private String marketStatus;
    private BigDecimal marketReferencePrice;
    private Integer marketTransactionCount;
    private Integer marketBasisLevel;
    private LocalDateTime marketCalculatedAt;
    private Integer cctvCountWithin500m;
    private Integer policeNearestDistanceMeters;
    private Integer childrenCountWithin500m;
    private Integer bellCountWithin500m;
    private Integer safetyLightCountWithin500m;
}
