package org.tajiro.property.infra.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.tajiro.seller.service.PropertyAggregationCoordinator;

@Component
@RequiredArgsConstructor
public class InfraBatchScheduler {

    private final PropertyAggregationCoordinator aggregationCoordinator;

    // 1분마다 실행 (PENDING 상태 건물 10개씩 자동 수집)
    @Scheduled(fixedDelayString = "${infra.aggregation.fixed-delay-ms:60000}")
    public void runInfraBatch() {
        aggregationCoordinator.processPendingInfrastructure(10);
    }
}
