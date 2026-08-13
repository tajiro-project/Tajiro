package org.tajiro.seller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SafetyPoiDTO {

    private Long safeId;
    private String safeName;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer distanceM;
    private Integer sortOrder;
    private String polygon;
}