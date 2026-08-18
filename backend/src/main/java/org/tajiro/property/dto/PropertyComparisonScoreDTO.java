package org.tajiro.property.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyComparisonScoreDTO {
    private Long propertyId;
    private Integer preferenceScore;
    private Integer commuteScore;
    private Integer costScore;
    private Integer infraScore;
    private Integer amenityScore;
}
