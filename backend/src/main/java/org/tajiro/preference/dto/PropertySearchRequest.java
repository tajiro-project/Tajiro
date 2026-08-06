package org.tajiro.preference.dto;

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
    private Long userId;
    private BigDecimal refLat;
    private BigDecimal refLng;
    private Integer radiusMeters;
    private Integer maxWorkplaceDistanceMeters;
    private List<String> propertyTypes;
    private List<String> tradeTypes;
    private List<String> floorPreference;
    private Integer minDeposit;
    private Integer maxDeposit;
    private Integer minMonthlyRent;
    private Integer maxMonthlyRent;
    private Integer minSellingPrice;
    private Integer maxSellingPrice;

    private BigDecimal minAreaM2;
    private BigDecimal maxAreaM2;

    private String desiredInfraCategories;
    private String desiredAmenityCategories;

    public boolean isHasPriceFilter() {
        return minDeposit != null || maxDeposit != null
                || minMonthlyRent != null || maxMonthlyRent != null
                || minSellingPrice != null || maxSellingPrice != null;
    }
}
