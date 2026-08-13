package org.tajiro.market.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tajiro.market.domain.MarketApiSource;
import org.tajiro.market.domain.MarketComparableTransactionVO;
import org.tajiro.market.domain.MarketEvaluationResult;
import org.tajiro.market.domain.MarketEvaluationStatus;
import org.tajiro.market.domain.MarketPropertyCategory;
import org.tajiro.market.domain.MarketPropertyVO;
import org.tajiro.market.domain.MarketSyncCoverageVO;
import org.tajiro.market.domain.MarketTradeType;
import org.tajiro.market.mapper.MarketTransactionMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MarketEvaluationService {

    private static final int HISTORY_MONTHS = 24;
    private static final DateTimeFormatter DEAL_MONTH_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMM");

    private final MarketTransactionMapper mapper;
    private final MarketPropertyCategoryResolver categoryResolver;
    private final MarketPriceConverter priceConverter;
    private final MarketEvaluationCalculator calculator;

    @Transactional
    public void evaluateProperties(List<Long> propertyIds) {
        if (propertyIds == null || propertyIds.isEmpty()) {
            return;
        }
        evaluate(mapper.findMarketProperties(propertyIds));
    }

    @Transactional
    public int evaluateAllActiveProperties() {
        List<MarketPropertyVO> properties = mapper.findAllActiveMarketProperties();
        evaluate(properties);
        return properties.size();
    }

    private void evaluate(List<MarketPropertyVO> properties) {
        Map<String, List<MarketComparableTransactionVO>> transactionCache = new HashMap<>();
        Map<String, List<MarketSyncCoverageVO>> coverageCache = new HashMap<>();
        LocalDate today = LocalDate.now();

        for (MarketPropertyVO property : properties) {
            mapper.updateEvaluation(evaluateOne(
                    property,
                    today,
                    transactionCache,
                    coverageCache));
        }
    }

    private MarketEvaluationResult evaluateOne(
            MarketPropertyVO property,
            LocalDate today,
            Map<String, List<MarketComparableTransactionVO>> transactionCache,
            Map<String, List<MarketSyncCoverageVO>> coverageCache) {
        if (property.getSggCode() == null || !property.getSggCode().matches("\\d{5}")) {
            return unavailable(property, MarketEvaluationStatus.REGION_CODE_MISSING, false);
        }

        List<MarketPropertyCategory> categories = categoryResolver.resolve(property);
        if (categories.isEmpty()) {
            return unavailable(property, MarketEvaluationStatus.SOURCE_UNMAPPED, false);
        }

        MarketTradeType tradeType;
        try {
            tradeType = MarketTradeType.fromPropertyTradeType(property.getTradeType());
        } catch (IllegalArgumentException e) {
            return unavailable(property, MarketEvaluationStatus.SOURCE_UNMAPPED, false);
        }

        if (tradeType == MarketTradeType.SALE
                && categories.size() == 1
                && categories.get(0) == MarketPropertyCategory.SINGLE_HOUSE) {
            return unavailable(property, MarketEvaluationStatus.SOURCE_UNMAPPED, false);
        }

        BigDecimal listingPrice = priceConverter.convert(property);
        if (listingPrice == null || listingPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return unavailable(property, MarketEvaluationStatus.SOURCE_UNMAPPED, false);
        }

        Optional<MarketEvaluationResult> coverageBlocker = coverageBlocker(
                property,
                categories,
                tradeType,
                today,
                coverageCache);
        if (coverageBlocker.isPresent()) {
            return coverageBlocker.get();
        }

        List<MarketEvaluationResult> calculated = new ArrayList<>();
        for (MarketPropertyCategory category : categories) {
            String cacheKey = category + "|" + tradeType + "|" + property.getSggCode();
            List<MarketComparableTransactionVO> transactions = transactionCache.computeIfAbsent(
                    cacheKey,
                    key -> mapper.findComparableTransactions(
                            category,
                            tradeType,
                            property.getSggCode(),
                            today.minusMonths(HISTORY_MONTHS)));
            calculator.calculate(property, listingPrice, transactions, today)
                    .ifPresent(calculated::add);
        }

        Optional<MarketEvaluationResult> best = calculated.stream()
                .min(Comparator
                        .comparing(MarketEvaluationResult::getBasisLevel)
                        .thenComparing(
                                MarketEvaluationResult::getTransactionCount,
                                Comparator.reverseOrder()));
        if (best.isPresent()) {
            return best.get();
        }

        return unavailable(property, MarketEvaluationStatus.DATA_INSUFFICIENT, false);
    }

    private Optional<MarketEvaluationResult> coverageBlocker(
            MarketPropertyVO property,
            List<MarketPropertyCategory> categories,
            MarketTradeType tradeType,
            LocalDate referenceDate,
            Map<String, List<MarketSyncCoverageVO>> coverageCache) {
        Set<MarketApiSource> sourceSet = new LinkedHashSet<>();
        for (MarketPropertyCategory category : categories) {
            sourceSet.addAll(MarketApiSource.defaultsFor(category, tradeType));
        }
        List<MarketApiSource> sources = new ArrayList<>(sourceSet);
        if (sources.isEmpty()) {
            return Optional.of(
                    unavailable(property, MarketEvaluationStatus.SOURCE_UNMAPPED, false));
        }

        String fromDealYm = YearMonth.from(referenceDate.minusMonths(HISTORY_MONTHS))
                .format(DEAL_MONTH_FORMAT);
        String cacheKey = sources.stream()
                .map(Enum::name)
                .sorted()
                .collect(Collectors.joining(","))
                + "|" + property.getSggCode()
                + "|" + fromDealYm;
        List<MarketSyncCoverageVO> coverage = coverageCache.computeIfAbsent(
                cacheKey,
                key -> mapper.findCoverage(sources, property.getSggCode(), fromDealYm));

        boolean blockingFailure = coverage.stream().anyMatch(item ->
                "FAILED".equals(item.getLastAttemptStatus()));
        if (blockingFailure) {
            return Optional.of(
                    unavailable(property, MarketEvaluationStatus.SYNC_ERROR, true));
        }

        long successfulMonths = coverage.stream()
                .filter(item -> "SUCCESS".equals(item.getLastAttemptStatus()))
                .filter(item -> item.getLastSuccessAt() != null)
                .map(item -> item.getSourceApi() + ":" + item.getDealYm())
                .distinct()
                .count();
        long expectedMonths = (long) sources.size() * (HISTORY_MONTHS + 1L);
        if (successfulMonths < expectedMonths) {
            return Optional.of(
                    unavailable(property, MarketEvaluationStatus.NOT_SYNCED, true));
        }
        return Optional.empty();
    }

    private MarketEvaluationResult unavailable(
            MarketPropertyVO property,
            MarketEvaluationStatus status,
            boolean preserveExistingScore) {
        return MarketEvaluationResult.builder()
                .propertyId(property.getId())
                .status(status)
                .evaluationScore(null)
                .referencePrice(null)
                .transactionCount(0)
                .basisLevel(null)
                .preserveExistingScore(preserveExistingScore)
                .build();
    }
}
