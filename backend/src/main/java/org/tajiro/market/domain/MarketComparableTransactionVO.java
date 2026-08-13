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
public class MarketComparableTransactionVO {
    private MarketApiSource sourceApi;
    private MarketPropertyCategory propertyCategory;
    private MarketTradeType tradeType;
    private String legalDongCode;
    private String umdName;
    private String jibun;
    private String buildingName;
    private String sourceBuildingCode;
    private BigDecimal exclusiveAreaM2;
    private BigDecimal totalFloorAreaM2;
    private LocalDate dealDate;
    private BigDecimal convertedPrice;

    public BigDecimal comparableArea() {
        return exclusiveAreaM2 != null ? exclusiveAreaM2 : totalFloorAreaM2;
    }
}
