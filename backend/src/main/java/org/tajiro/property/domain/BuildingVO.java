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
public class BuildingVO {
    private Long id;
    private String buildingCode;
    private String roadAddress;
    private String jibunAddress;
    private String bldNm;
    private String dongNm;
    private String pnu;
    private BigDecimal latitude;  // 매물 위치 기준 위도
    private BigDecimal longitude; // 매물 위치 기준 경도
}