package org.tajiro.seller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SafetyAggregationDTO {
    private String safeCategory;
    private Integer countWithin500m;
    private Integer nearestDistanceMeters;
}