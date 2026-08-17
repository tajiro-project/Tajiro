package org.tajiro.seller.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.tajiro.seller.service.PropertyAggregationCoordinator;

@Component
@RequiredArgsConstructor
@Log4j2
public class PropertyAggregationListener {

    private final PropertyAggregationCoordinator aggregationCoordinator;

    @Async("propertyAggregationExecutor")
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT,
            fallbackExecution = false
    )
    public void handle(PropertyRegisteredEvent event) {
        if (event.getBuildingId() == null) {
            log.warn(
                    "매물 등록 후 집계 대기 - propertyId={}, buildingId=null",
                    event.getPropertyId()
            );
            return;
        }

        aggregationCoordinator.aggregateRegisteredProperty(event);
    }
}
