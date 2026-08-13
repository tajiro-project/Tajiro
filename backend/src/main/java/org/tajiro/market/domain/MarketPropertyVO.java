package org.tajiro.market.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketPropertyVO {
    private Long id;
    private String propertyType;
    private String tradeType;
    private Integer deposit;
    private Integer monthlyRent;
    private BigDecimal areaM2;
    private String title;
    private String address;
    private String propertyDescription;
    private String configuredCategory;
    private String buildingName;
    private String buildingDong;
    private String pnu;
    private String roadAddress;
    private String jibunAddress;
    private String sggCode;
}
