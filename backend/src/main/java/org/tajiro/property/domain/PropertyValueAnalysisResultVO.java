package org.tajiro.property.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyValueAnalysisResultVO {
    private Long id;
    private Long userId;
    private Long propertyId;
    private Integer workplaceDistanceMeters;
    private LocalDateTime computedAt;
    private Integer recommendScore;
}
