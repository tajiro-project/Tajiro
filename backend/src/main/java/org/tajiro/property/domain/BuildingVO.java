package org.tajiro.property.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuildingVO {
    private Long id;
    private String buildingCode;
    private String roadAddress;
    private String jibunAddress;
    private String bldNm;
    private String pnu;
    private BigDecimal latitude;
    private BigDecimal longitude;
}
