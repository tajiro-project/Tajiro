package org.tajiro.seller.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.tajiro.property.service.BuildingInfraBatchService;
import org.tajiro.seller.event.PropertyRegisteredEvent;
import org.tajiro.seller.mapper.BuildingAggregationMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class PropertyAggregationCoordinator {

    private final BuildingAggregationMapper buildingAggregationMapper;
    private final BuildingInfraBatchService buildingInfraBatchService;
    private final SafetyAggregationService safetyAggregationService;

    public void aggregateRegisteredProperty(PropertyRegisteredEvent event) {
        Long propertyId = event.getPropertyId();
        Long buildingId = event.getBuildingId();

        aggregateInfrastructure(propertyId, buildingId);
        aggregateSafety(propertyId, buildingId);
    }

    public void processPendingInfrastructure(int limit) {
        validateLimit(limit);
        List<Long> buildingIds =
                buildingAggregationMapper.findPendingInfrastructureBuildingIds(limit);
        for (Long buildingId : buildingIds) {
            aggregateInfrastructure(null, buildingId);
        }
    }

    public void processPendingSafety(int limit) {
        validateLimit(limit);
        List<Long> buildingIds =
                buildingAggregationMapper.findPendingSafetyBuildingIds(limit);
        for (Long buildingId : buildingIds) {
            aggregateSafety(null, buildingId);
        }
    }

    public boolean aggregateInfrastructure(Long propertyId, Long buildingId) {
        if (buildingId == null) {
            log.debug(
                    "인프라 집계 생략 - propertyId={}, buildingId=null",
                    propertyId
            );
            return false;
        }

        boolean claimed = false;
        try {
            if (buildingAggregationMapper.claimInfrastructure(buildingId) != 1) {
                log.debug(
                        "인프라 집계 생략 - propertyId={}, buildingId={}, reason=not-pending",
                        propertyId,
                        buildingId
                );
                return false;
            }
            claimed = true;
            buildingInfraBatchService.processSingleBuilding(buildingId);
            ensureUpdated(
                    buildingAggregationMapper.completeInfrastructure(buildingId),
                    "인프라 집계 완료 상태 변경",
                    buildingId
            );
            log.info(
                    "인프라 집계 완료 - propertyId={}, buildingId={}",
                    propertyId,
                    buildingId
            );
            return true;
        } catch (RuntimeException exception) {
            if (claimed) {
                markInfrastructureFailed(buildingId, exception);
            }
            log.error(
                    "인프라 집계 실패 - propertyId={}, buildingId={}",
                    propertyId,
                    buildingId,
                    exception
            );
            return false;
        }
    }

    public boolean aggregateSafety(Long propertyId, Long buildingId) {
        if (buildingId == null) {
            log.debug(
                    "안전 집계 생략 - propertyId={}, buildingId=null",
                    propertyId
            );
            return false;
        }

        boolean claimed = false;
        try {
            if (buildingAggregationMapper.claimSafety(buildingId) != 1) {
                log.debug(
                        "안전 집계 생략 - propertyId={}, buildingId={}, reason=not-pending",
                        propertyId,
                        buildingId
                );
                return false;
            }
            claimed = true;
            safetyAggregationService.aggregateBuilding(buildingId);
            ensureUpdated(
                    buildingAggregationMapper.completeSafety(buildingId),
                    "안전 집계 완료 상태 변경",
                    buildingId
            );
            log.info(
                    "안전 집계 완료 - propertyId={}, buildingId={}",
                    propertyId,
                    buildingId
            );
            return true;
        } catch (RuntimeException exception) {
            if (claimed) {
                markSafetyFailed(buildingId, exception);
            }
            log.error(
                    "안전 집계 실패 - propertyId={}, buildingId={}",
                    propertyId,
                    buildingId,
                    exception
            );
            return false;
        }
    }

    private void ensureUpdated(int updated, String action, Long buildingId) {
        if (updated != 1) {
            throw new IllegalStateException(
                    action + "에 실패했습니다. buildingId=" + buildingId
            );
        }
    }

    private void markInfrastructureFailed(Long buildingId, RuntimeException cause) {
        try {
            buildingAggregationMapper.failInfrastructure(buildingId);
        } catch (RuntimeException statusException) {
            cause.addSuppressed(statusException);
        }
    }

    private void markSafetyFailed(Long buildingId, RuntimeException cause) {
        try {
            buildingAggregationMapper.failSafety(buildingId);
        } catch (RuntimeException statusException) {
            cause.addSuppressed(statusException);
        }
    }

    private void validateLimit(int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("limit은 1 이상이어야 합니다.");
        }
    }
}
