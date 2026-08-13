package org.tajiro.market.domain;

public enum MarketTradeType {
    SALE,
    JEONSE,
    MONTHLY_RENT;

    public static MarketTradeType fromPropertyTradeType(String tradeType) {
        if ("매매".equals(tradeType)) {
            return SALE;
        }
        if ("전세".equals(tradeType)) {
            return JEONSE;
        }
        if ("월세".equals(tradeType)) {
            return MONTHLY_RENT;
        }
        throw new IllegalArgumentException("지원하지 않는 거래 유형입니다: " + tradeType);
    }
}
