package org.tajiro.property.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuildingVO {

    private Long id;
    private String buildingCode;   // DB: building_code
    private String roadAddress;    // DB: road_address
    private String jibunAddress;   // DB: jibun_address
    private String bldNm;          // DB: bld_nm
    private String dongNm;         // DB: dong_nm
    private String pnu;            // DB: pnu (char 19)
    private BigDecimal latitude;   // DB: latitude (decimal 10,7)
    private BigDecimal longitude;  // DB: longitude (decimal 10,7)
    private String infraStatus;    // DB: infra_status
    private String safetyStatus;   // DB: safety_status
}
