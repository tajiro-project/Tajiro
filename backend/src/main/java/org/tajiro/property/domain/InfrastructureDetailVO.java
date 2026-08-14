package org.tajiro.property.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class InfrastructureDetailVO {

    private Long id;
    private Long infraId;
    private String infraName;
    private String roadAddress;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer distanceM;
    private Integer sortOrder;
}