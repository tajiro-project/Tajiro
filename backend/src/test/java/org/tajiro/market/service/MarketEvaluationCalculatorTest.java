package org.tajiro.market.service;

import org.junit.jupiter.api.Test;
import org.tajiro.market.domain.MarketComparableTransactionVO;
import org.tajiro.market.domain.MarketEvaluationResult;
import org.tajiro.market.domain.MarketPropertyVO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MarketEvaluationCalculatorTest {

    private final MarketEvaluationCalculator calculator =
            new MarketEvaluationCalculator();

    @Test
    void calculatesSignedPercentAgainstSameBuildingMedian() {
        LocalDate today = LocalDate.of(2026, 8, 12);
        MarketPropertyVO property = MarketPropertyVO.builder()
                .id(10L)
                .areaM2(new BigDecimal("84.9"))
                .buildingName("인왕산 아이파크 아파트")
                .pnu("1111018700000600000")
                .jibunAddress("서울 종로구 무악동 60")
                .build();
        List<MarketComparableTransactionVO> transactions = Arrays.asList(
                transaction("48000", "83.0", today.minusMonths(2)),
                transaction("50000", "84.9", today.minusMonths(3)),
                transaction("52000", "86.0", today.minusMonths(5)));

        MarketEvaluationResult result = calculator.calculate(
                        property,
                        new BigDecimal("55000"),
                        transactions,
                        today)
                .orElseThrow(AssertionError::new);

        assertEquals(new BigDecimal("50000.00"), result.getReferencePrice());
        assertEquals(new BigDecimal("10.00"), result.getEvaluationScore());
        assertEquals(1, result.getBasisLevel());
        assertEquals(3, result.getTransactionCount());
    }

    @Test
    void requiresAtLeastThreeComparableTransactions() {
        LocalDate today = LocalDate.of(2026, 8, 12);
        MarketPropertyVO property = MarketPropertyVO.builder()
                .id(10L)
                .areaM2(new BigDecimal("84.9"))
                .buildingName("테스트아파트")
                .pnu("1111018700000600000")
                .jibunAddress("서울 종로구 무악동 60")
                .build();

        assertFalse(calculator.calculate(
                property,
                new BigDecimal("50000"),
                Arrays.asList(
                        transaction("48000", "84.0", today.minusMonths(2)),
                        transaction("50000", "85.0", today.minusMonths(3))),
                today).isPresent());
    }

    @Test
    void computesMedianForEvenAndOddCounts() {
        assertEquals(new BigDecimal("20"), calculator.median(Arrays.asList(
                new BigDecimal("30"),
                new BigDecimal("10"),
                new BigDecimal("20"))));
        assertEquals(new BigDecimal("25.00"), calculator.median(Arrays.asList(
                new BigDecimal("40"),
                new BigDecimal("10"),
                new BigDecimal("20"),
                new BigDecimal("30"))));
    }

    private MarketComparableTransactionVO transaction(
            String price,
            String area,
            LocalDate dealDate) {
        return MarketComparableTransactionVO.builder()
                .legalDongCode("18700")
                .umdName("무악동")
                .jibun("60")
                .buildingName("인왕산아이파크")
                .exclusiveAreaM2(new BigDecimal(area))
                .convertedPrice(new BigDecimal(price))
                .dealDate(dealDate)
                .build();
    }
}
