package org.tajiro.preference.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HousingPreferenceVO {
    private Long userId;
    private String housingTypes;
    private String tradeTypes;
    private Integer minDeposit;
    private Integer maxDeposit;
    private Integer minMonthlyRent;
    private Integer maxMonthlyRent;
    private Integer minSellingPrice;
    private Integer maxSellingPrice;
    private BigDecimal minArea;
    private BigDecimal maxArea;
    private String floorPreference;
    private Integer maxWorkplaceDistanceMeters;
    private String desiredInfraCategories;
    private String desiredAmenityCategories;
    private Boolean hasCar;
    private BigDecimal workplaceLatitude;
    private BigDecimal workplaceLongitude;
    private String workplaceAddress;
}
