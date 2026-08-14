package org.tajiro.market.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tajiro.market.client.PublicDataTransactionClient;
import org.tajiro.market.domain.ActualTransactionVO;
import org.tajiro.market.domain.MarketApiSource;
import org.tajiro.market.domain.MarketPropertyCategory;
import org.tajiro.market.domain.MarketSyncSummary;
import org.tajiro.market.domain.MarketSyncTarget;
import org.tajiro.market.domain.MarketTradeType;
import org.tajiro.market.mapper.MarketTransactionMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketSyncService {

    private static final DateTimeFormatter DEAL_MONTH_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMM");

    private final PublicDataTransactionClient apiClient;
    private final MarketTransactionMapper mapper;
    private final MarketPropertyCategoryResolver categoryResolver;
    private final MarketTransactionStore transactionStore;

    public boolean isApiConfigured() {
        return apiClient.isConfigured();
    }

    public LocalDateTime getLatestSuccessfulSyncAt() {
        return mapper.findLatestSuccessfulSyncAt();
    }

    public MarketSyncSummary syncRegisteredMarkets(int historyMonths, int refreshMonths) {
        if (!apiClient.isConfigured()) {
            throw new IllegalStateException("공공데이터포털 실거래가 API 키가 설정되지 않았습니다.");
        }
        int safeHistoryMonths = Math.max(1, Math.min(historyMonths, 60));
        int safeRefreshMonths = Math.max(0, Math.min(refreshMonths, safeHistoryMonths));

        MarketSyncSummary summary = MarketSyncSummary.builder().build();
        List<SourceRegion> sourceRegions = resolveSourceRegions(mapper.findSyncTargets());
        YearMonth currentMonth = YearMonth.now();

        for (SourceRegion sourceRegion : sourceRegions) {
            // rolling N개월에는 시작 월의 일부가 포함되므로 calendar month는 N+1개를 받는다.
            for (int offset = 0; offset <= safeHistoryMonths; offset++) {
                YearMonth dealMonth = currentMonth.minusMonths(offset);
                String dealYm = dealMonth.format(DEAL_MONTH_FORMAT);
                summary.setRequestedMonths(summary.getRequestedMonths() + 1);

                boolean refresh = offset < safeRefreshMonths;
                if (!refresh && mapper.hasSuccessfulCoverage(
                        sourceRegion.source,
                        sourceRegion.sggCode,
                        dealYm)) {
                    summary.setSkippedCoveredMonths(summary.getSkippedCoveredMonths() + 1);
                    continue;
                }

                try {
                    List<ActualTransactionVO> transactions = apiClient.fetch(
                            sourceRegion.source,
                            sourceRegion.sggCode,
                            dealMonth);
                    transactionStore.storeSuccess(
                            sourceRegion.source,
                            sourceRegion.sggCode,
                            dealYm,
                            transactions);
                    summary.setSuccessfulMonths(summary.getSuccessfulMonths() + 1);
                    summary.setStoredTransactions(
                            summary.getStoredTransactions() + transactions.size());
                } catch (RuntimeException e) {
                    log.warn("실거래가 동기화 실패 source={}, sggCode={}, dealYm={}, error={}",
                            sourceRegion.source,
                            sourceRegion.sggCode,
                            dealYm,
                            e.getMessage());
                    transactionStore.storeFailure(
                            sourceRegion.source,
                            sourceRegion.sggCode,
                            dealYm,
                            e.getMessage());
                    summary.setFailedMonths(summary.getFailedMonths() + 1);
                }
            }
        }

        LocalDate cutoffDate = LocalDate.now().minusMonths(safeHistoryMonths);
        mapper.deleteTransactionsBefore(cutoffDate);
        mapper.deleteCoverageBefore(YearMonth.from(cutoffDate).format(DEAL_MONTH_FORMAT));

        return summary;
    }

    private List<SourceRegion> resolveSourceRegions(List<MarketSyncTarget> targets) {
        Map<String, SourceRegion> unique = new LinkedHashMap<>();
        for (MarketSyncTarget target : targets) {
            MarketTradeType tradeType;
            try {
                tradeType = MarketTradeType.fromPropertyTradeType(target.getTradeType());
            } catch (IllegalArgumentException e) {
                log.warn("지원하지 않는 매물 거래유형을 건너뜁니다: {}", target.getTradeType());
                continue;
            }

            for (MarketPropertyCategory category : categoryResolver.resolve(target)) {
                for (MarketApiSource source : MarketApiSource.defaultsFor(category, tradeType)) {
                    SourceRegion sourceRegion = new SourceRegion(source, target.getSggCode());
                    unique.put(source.name() + ":" + target.getSggCode(), sourceRegion);
                }
            }
        }
        return new ArrayList<>(unique.values());
    }

    private static final class SourceRegion {
        private final MarketApiSource source;
        private final String sggCode;

        private SourceRegion(MarketApiSource source, String sggCode) {
            this.source = source;
            this.sggCode = sggCode;
        }
    }
}
