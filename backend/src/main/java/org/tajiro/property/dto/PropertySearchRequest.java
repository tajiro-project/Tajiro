package org.tajiro.property.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertySearchRequest {
    private List<String> tradeTypes;
    private List<String> propertyTypes;
    private Integer minDeposit;
    private Integer maxDeposit;
    private Integer minMonthlyRent;
    private Integer maxMonthlyRent;
    private BigDecimal minAreaM2;
    private BigDecimal maxAreaM2;
    private List<String> floorPreference;
    private BigDecimal workLat;
    private BigDecimal workLng;
    private Integer maxWorkplaceDistanceMeters;
}
