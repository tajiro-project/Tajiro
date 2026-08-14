package org.tajiro.property.infra.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.tajiro.property.service.BuildingInfraBatchService;

@Component
@RequiredArgsConstructor
public class InfraBatchScheduler {

    private final BuildingInfraBatchService batchService;

    // 1분마다 실행 (미수집 건물 5개씩 자동 수집)
    @Scheduled(fixedDelay = 60000)
    public void runInfraBatch() {
        batchService.processPendingBuildings(10);
    }
}