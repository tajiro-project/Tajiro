package org.tajiro.seller.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
@Component
@RequiredArgsConstructor
@Log4j2
public class SafetyAggregationRunner {

    private final PropertyAggregationCoordinator aggregationCoordinator;


    /*
     * 한 번에 처리할 건물 개수
     *
     * application.properties에 값이 없으면
     * 기본 20개
     */
    @Value("${safety.aggregation.batch-size:20}")
    private int batchSize;


    /*
     * 기본값
     *
     * 서버 시작 10초 후 첫 실행
     * 이후 이전 작업 종료 후 60초 뒤 다시 실행
     */
    @Scheduled(
            fixedDelayString =
                    "${safety.aggregation.fixed-delay-ms:60000}",

            initialDelayString =
                    "${safety.aggregation.initial-delay-ms:10000}"
    )
    public void run() {
        log.info("안전 집계 배치 시작 - batchSize={}", batchSize);
        aggregationCoordinator.processPendingSafety(batchSize);
        log.info("안전 집계 배치 종료 - batchSize={}", batchSize);
    }
}
