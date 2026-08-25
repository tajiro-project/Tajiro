package org.tajiro.market.service;

import org.junit.jupiter.api.Test;
import org.tajiro.market.domain.ActualTransactionVO;
import org.tajiro.market.domain.MarketTradeType;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MarketPriceConverterTest {

    private final MarketPriceConverter converter =
            new MarketPriceConverter(new BigDecimal("0.0475"));

    @Test
    void convertsSaleJeonseAndMonthlyRentToComparablePrices() {
        ActualTransactionVO sale = ActualTransactionVO.builder()
                .tradeType(MarketTradeType.SALE)
                .dealAmount(50_000)
                .build();
        ActualTransactionVO jeonse = ActualTransactionVO.builder()
                .tradeType(MarketTradeType.JEONSE)
                .deposit(30_000)
                .monthlyRent(0)
                .build();
        ActualTransactionVO monthly = ActualTransactionVO.builder()
                .tradeType(MarketTradeType.MONTHLY_RENT)
                .deposit(10_000)
                .monthlyRent(50)
                .build();

        assertEquals(new BigDecimal("50000"), converter.convert(sale));
        assertEquals(new BigDecimal("30000.00"), converter.convert(jeonse));
        assertEquals(new BigDecimal("22000.00"), converter.convert(monthly));
    }
}
