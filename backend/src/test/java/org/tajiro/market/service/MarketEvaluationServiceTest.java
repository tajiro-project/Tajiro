package org.tajiro.market.service;

import org.junit.jupiter.api.Test;
import org.tajiro.market.domain.ActualTransactionVO;
import org.tajiro.market.domain.MarketApiSource;
import org.tajiro.market.domain.MarketComparableTransactionVO;
import org.tajiro.market.domain.MarketEvaluationResult;
import org.tajiro.market.domain.MarketEvaluationStatus;
import org.tajiro.market.domain.MarketPropertyCategory;
import org.tajiro.market.domain.MarketPropertyVO;
import org.tajiro.market.domain.MarketSyncCoverageVO;
import org.tajiro.market.domain.MarketSyncTarget;
import org.tajiro.market.domain.MarketTradeType;
import org.tajiro.market.mapper.MarketTransactionMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MarketEvaluationServiceTest {

    @Test
    void latestSyncFailurePreservesScoreEvenWhenAnOlderSuccessExists() {
        FakeMapper mapper = new FakeMapper(property(), successfulCoverage(25));
        mapper.coverage.get(0).setLastAttemptStatus("FAILED");

        service(mapper).evaluateProperties(Collections.singletonList(1L));

        assertEquals(MarketEvaluationStatus.SYNC_ERROR, mapper.updated.getStatus());
        assertTrue(mapper.updated.isPreserveExistingScore());
        assertEquals(0, mapper.comparableQueryCount);
    }

    @Test
    void missingCoveragePreservesScoreAsNotSynced() {
        FakeMapper mapper = new FakeMapper(property(), successfulCoverage(23));

        service(mapper).evaluateProperties(Collections.singletonList(1L));

        assertEquals(MarketEvaluationStatus.NOT_SYNCED, mapper.updated.getStatus());
        assertTrue(mapper.updated.isPreserveExistingScore());
        assertEquals(0, mapper.comparableQueryCount);
    }

    @Test
    void completeCoverageWithTooFewComparablesClearsScoreAsInsufficient() {
        FakeMapper mapper = new FakeMapper(property(), successfulCoverage(25));
        mapper.comparables = Collections.emptyList();

        service(mapper).evaluateProperties(Collections.singletonList(1L));

        assertEquals(MarketEvaluationStatus.DATA_INSUFFICIENT, mapper.updated.getStatus());
        assertFalse(mapper.updated.isPreserveExistingScore());
        assertEquals(1, mapper.comparableQueryCount);
    }

    private MarketEvaluationService service(FakeMapper mapper) {
        return new MarketEvaluationService(
                mapper,
                new MarketPropertyCategoryResolver(),
                new MarketPriceConverter(new BigDecimal("0.05")),
                new MarketEvaluationCalculator());
    }

    private MarketPropertyVO property() {
        return MarketPropertyVO.builder()
                .id(1L)
                .propertyType("아파트")
                .tradeType("매매")
                .deposit(11000)
                .monthlyRent(0)
                .areaM2(new BigDecimal("50"))
                .configuredCategory("APARTMENT")
                .buildingName("테스트아파트")
                .pnu("3011012345100010000")
                .jibunAddress("대전광역시 동구 테스트동 1")
                .sggCode("30110")
                .build();
    }

    private List<MarketSyncCoverageVO> successfulCoverage(int count) {
        List<MarketSyncCoverageVO> result = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMM");
        for (int offset = 0; offset < count; offset++) {
            result.add(MarketSyncCoverageVO.builder()
                    .sourceApi(MarketApiSource.APT_SALE_DETAIL)
                    .sggCode("30110")
                    .dealYm(YearMonth.now().minusMonths(offset).format(formatter))
                    .lastAttemptStatus("SUCCESS")
                    .lastSuccessAt(LocalDateTime.now())
                    .lastAttemptAt(LocalDateTime.now())
                    .build());
        }
        return result;
    }

    private static class FakeMapper implements MarketTransactionMapper {
        private final MarketPropertyVO property;
        private final List<MarketSyncCoverageVO> coverage;
        private List<MarketComparableTransactionVO> comparables = Collections.emptyList();
        private MarketEvaluationResult updated;
        private int comparableQueryCount;

        private FakeMapper(
                MarketPropertyVO property,
                List<MarketSyncCoverageVO> coverage) {
            this.property = property;
            this.coverage = coverage;
        }

        @Override
        public List<MarketPropertyVO> findMarketProperties(List<Long> propertyIds) {
            return Collections.singletonList(property);
        }

        @Override
        public List<MarketComparableTransactionVO> findComparableTransactions(
                MarketPropertyCategory propertyCategory,
                MarketTradeType tradeType,
                String sggCode,
                LocalDate fromDate) {
            comparableQueryCount++;
            return comparables;
        }

        @Override
        public List<MarketSyncCoverageVO> findCoverage(
                List<MarketApiSource> sources,
                String sggCode,
                String fromDealYm) {
            return coverage;
        }

        @Override
        public int updateEvaluation(MarketEvaluationResult result) {
            updated = result;
            return 1;
        }

        @Override
        public int deleteTransactionsForMonth(
                MarketApiSource sourceApi,
                String sggCode,
                String dealYm) {
            return 0;
        }

        @Override
        public int deleteTransactionsBefore(LocalDate cutoffDate) {
            return 0;
        }

        @Override
        public int deleteCoverageBefore(String cutoffDealYm) {
            return 0;
        }

        @Override
        public int upsertTransactions(List<ActualTransactionVO> transactions) {
            return 0;
        }

        @Override
        public int markSyncSuccess(
                MarketApiSource sourceApi,
                String sggCode,
                String dealYm,
                int totalCount) {
            return 0;
        }

        @Override
        public int markSyncFailure(
                MarketApiSource sourceApi,
                String sggCode,
                String dealYm,
                String lastError) {
            return 0;
        }

        @Override
        public boolean hasSuccessfulCoverage(
                MarketApiSource sourceApi,
                String sggCode,
                String dealYm) {
            return false;
        }

        @Override
        public List<MarketSyncTarget> findSyncTargets() {
            return Collections.emptyList();
        }

        @Override
        public List<MarketPropertyVO> findAllActiveMarketProperties() {
            return Collections.emptyList();
        }
    }
}
