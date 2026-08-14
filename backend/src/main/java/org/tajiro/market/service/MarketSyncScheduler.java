package org.tajiro.market.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.tajiro.market.domain.MarketSyncSummary;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@RequiredArgsConstructor
@Slf4j
public class MarketSyncScheduler {

    private final MarketSyncService syncService;
    private final MarketEvaluationService evaluationService;
    private final AtomicBoolean running = new AtomicBoolean(false);

    @Value("${market.sync.enabled:false}")
    private boolean enabled;

    @Value("${market.sync.history-months:24}")
    private int historyMonths;

    @Value("${market.sync.refresh-months:3}")
    private int refreshMonths;

    @Value("${market.sync.minimum-interval-ms:${market.sync.fixed-delay-ms:86400000}}")
    private long minimumIntervalMs;

    @Scheduled(
            initialDelayString = "${market.sync.initial-delay-ms:60000}",
            fixedDelayString = "${market.sync.check-delay-ms:3600000}")
    public void synchronize() {
        if (!enabled) {
            return;
        }
        if (!syncService.isApiConfigured()) {
            log.warn("market.sync.enabled=true이지만 실거래가 API 키가 없어 동기화를 건너뜁니다.");
            return;
        }
        LocalDateTime latestSyncAt = syncService.getLatestSuccessfulSyncAt();
        Duration minimumInterval = Duration.ofMillis(Math.max(0L, minimumIntervalMs));
        if (latestSyncAt != null
                && latestSyncAt.plus(minimumInterval).isAfter(LocalDateTime.now())) {
            log.info("실거래가 최근 동기화 후 최소 간격이 지나지 않아 건너뜁니다. latestSyncAt={}, nextSyncAt={}",
                    latestSyncAt,
                    latestSyncAt.plus(minimumInterval));
            return;
        }

        if (!running.compareAndSet(false, true)) {
            log.info("실거래가 동기화가 이미 실행 중이어서 이번 실행을 건너뜁니다.");
            return;
        }

        try {
            MarketSyncSummary summary = syncService.syncRegisteredMarkets(
                    historyMonths,
                    refreshMonths);
            int evaluatedProperties = evaluationService.evaluateAllActiveProperties();
            log.info("실거래가 동기화/평가 완료 summary={}, evaluatedProperties={}",
                    summary,
                    evaluatedProperties);
        } catch (RuntimeException e) {
            log.error("실거래가 정기 동기화 중 오류가 발생했습니다.", e);
        } finally {
            running.set(false);
        }
    }
}
