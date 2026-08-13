package org.tajiro.market.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActualTransactionVO {
    private Long id;
    private MarketApiSource sourceApi;
    private String sourceUniqueKey;
    private MarketPropertyCategory propertyCategory;
    private MarketTradeType tradeType;
    private String sggCode;
    private String sourceDealYm;
    private String legalDongCode;
    private String umdName;
    private String jibun;
    private String buildingName;
    private String sourceBuildingCode;
    private String buildingDong;
    private String houseType;
    private BigDecimal exclusiveAreaM2;
    private BigDecimal totalFloorAreaM2;
    private BigDecimal landAreaM2;
    private Integer floor;
    private Integer buildYear;
    private LocalDate dealDate;
    private Integer dealAmount;
    private Integer deposit;
    private Integer monthlyRent;
    private BigDecimal convertedPrice;
    private Boolean canceled;
    private String cancellationDate;
}
