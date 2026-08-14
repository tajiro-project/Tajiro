package org.tajiro.seller.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuildingCreateVO {
    private Long id;
    private String buildingCode;
    private String roadAddress;
    private String jibunAddress;
    private String bldNm;
    private String dongNm;
    private String pnu;
    private BigDecimal latitude;
    private BigDecimal longitude;
}
