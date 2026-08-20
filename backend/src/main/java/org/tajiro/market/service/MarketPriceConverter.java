package org.tajiro.market.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.tajiro.market.domain.ActualTransactionVO;
import org.tajiro.market.domain.MarketPropertyVO;
import org.tajiro.market.domain.MarketTradeType;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class MarketPriceConverter {

    private static final BigDecimal MONTHS_PER_YEAR = BigDecimal.valueOf(12);

    private final BigDecimal rentConversionRate;

    public MarketPriceConverter(
            @Value("${market.rent-conversion-rate:0.05}") BigDecimal rentConversionRate) {
        if (rentConversionRate == null
                || rentConversionRate.compareTo(BigDecimal.ZERO) <= 0
                || rentConversionRate.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException(
                    "market.rent-conversion-rate는 0보다 크고 1 이하여야 합니다.");
        }
        this.rentConversionRate = rentConversionRate;
    }

    public BigDecimal convert(ActualTransactionVO transaction) {
        if (transaction == null || transaction.getTradeType() == null) {
            return null;
        }
        if (transaction.getTradeType() == MarketTradeType.SALE) {
            return decimal(transaction.getDealAmount());
        }
        return convertRent(transaction.getDeposit(), transaction.getMonthlyRent());
    }

    public BigDecimal convert(MarketPropertyVO property) {
        if (property == null) {
            return null;
        }
        MarketTradeType tradeType;
        try {
            tradeType = MarketTradeType.fromPropertyTradeType(property.getTradeType());
        } catch (IllegalArgumentException e) {
            return null;
        }
        if (tradeType == MarketTradeType.SALE || tradeType == MarketTradeType.JEONSE) {
            return decimal(property.getDeposit());
        }
        return convertRent(property.getDeposit(), property.getMonthlyRent());
    }

    public BigDecimal convertRent(Integer deposit, Integer monthlyRent) {
        BigDecimal depositAmount = decimal(deposit);
        if (depositAmount == null) {
            return null;
        }
        if (monthlyRent == null || monthlyRent == 0) {
            return depositAmount.setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal annualRent = BigDecimal.valueOf(monthlyRent)
                .multiply(MONTHS_PER_YEAR);
        return depositAmount.add(
                        annualRent.divide(rentConversionRate, 2, RoundingMode.HALF_UP))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getRentConversionRate() {
        return rentConversionRate;
    }

    private BigDecimal decimal(Integer value) {
        return value == null ? null : BigDecimal.valueOf(value);
    }
}
