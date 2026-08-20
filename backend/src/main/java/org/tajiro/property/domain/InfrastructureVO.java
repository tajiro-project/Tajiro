package org.tajiro.property.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class InfrastructureVO {

    private Long id;
    private Long buildingId;
    private String infraCategory;
    private Integer countWithin2000m;
    private Integer nearestDistanceMeters;
    private Integer nearestWalkMinutes;
    private Boolean isSaturated;
    private LocalDateTime computedAt;
}