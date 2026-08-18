package org.tajiro.seller.service;

import org.junit.jupiter.api.Test;
import org.tajiro.property.domain.BuildingVO;
import org.tajiro.property.service.BuildingInfraBatchService;
import org.tajiro.seller.event.PropertyRegisteredEvent;
import org.tajiro.seller.mapper.BuildingAggregationMapper;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PropertyAggregationCoordinatorTest {

    @Test
    void aggregateRegisteredPropertyCompletesBothAggregations() {
        StubBuildingAggregationMapper mapper = new StubBuildingAggregationMapper();
        StubInfrastructureService infrastructureService = new StubInfrastructureService();
        StubSafetyService safetyService = new StubSafetyService();
        PropertyAggregationCoordinator coordinator = new PropertyAggregationCoordinator(
                mapper,
                infrastructureService,
                safetyService
        );

        coordinator.aggregateRegisteredProperty(new PropertyRegisteredEvent(11L, 22L));

        assertEquals(List.of(22L), infrastructureService.processedBuildingIds);
        assertEquals(List.of(22L), safetyService.processedBuildingIds);
        assertEquals(1, mapper.infrastructureCompleted);
        assertEquals(1, mapper.safetyCompleted);
        assertEquals(0, mapper.infrastructureFailed);
        assertEquals(0, mapper.safetyFailed);
    }

    @Test
    void infrastructureFailureDoesNotStopSafetyAggregation() {
        StubBuildingAggregationMapper mapper = new StubBuildingAggregationMapper();
        StubInfrastructureService infrastructureService = new StubInfrastructureService();
        infrastructureService.fail = true;
        StubSafetyService safetyService = new StubSafetyService();
        PropertyAggregationCoordinator coordinator = new PropertyAggregationCoordinator(
                mapper,
                infrastructureService,
                safetyService
        );

        coordinator.aggregateRegisteredProperty(new PropertyRegisteredEvent(11L, 22L));

        assertEquals(1, mapper.infrastructureFailed);
        assertEquals(0, mapper.infrastructureCompleted);
        assertEquals(1, mapper.safetyCompleted);
        assertEquals(List.of(22L), safetyService.processedBuildingIds);
    }

    @Test
    void alreadyClaimedBuildingIsSkipped() {
        StubBuildingAggregationMapper mapper = new StubBuildingAggregationMapper();
        mapper.infrastructureClaimed = false;
        mapper.safetyClaimed = false;
        StubInfrastructureService infrastructureService = new StubInfrastructureService();
        StubSafetyService safetyService = new StubSafetyService();
        PropertyAggregationCoordinator coordinator = new PropertyAggregationCoordinator(
                mapper,
                infrastructureService,
                safetyService
        );

        boolean infrastructureProcessed = coordinator.aggregateInfrastructure(11L, 22L);
        boolean safetyProcessed = coordinator.aggregateSafety(11L, 22L);

        assertFalse(infrastructureProcessed);
        assertFalse(safetyProcessed);
        assertTrue(infrastructureService.processedBuildingIds.isEmpty());
        assertTrue(safetyService.processedBuildingIds.isEmpty());
    }

    @Test
    void infrastructureClaimFailureDoesNotStopSafetyAggregation() {
        StubBuildingAggregationMapper mapper = new StubBuildingAggregationMapper();
        mapper.infrastructureClaimFails = true;
        StubInfrastructureService infrastructureService = new StubInfrastructureService();
        StubSafetyService safetyService = new StubSafetyService();
        PropertyAggregationCoordinator coordinator = new PropertyAggregationCoordinator(
                mapper,
                infrastructureService,
                safetyService
        );

        coordinator.aggregateRegisteredProperty(new PropertyRegisteredEvent(11L, 22L));

        assertTrue(infrastructureService.processedBuildingIds.isEmpty());
        assertEquals(List.of(22L), safetyService.processedBuildingIds);
        assertEquals(1, mapper.safetyCompleted);
    }

    private static class StubBuildingAggregationMapper
            implements BuildingAggregationMapper {

        private boolean infrastructureClaimed = true;
        private boolean safetyClaimed = true;
        private boolean infrastructureClaimFails;
        private int infrastructureCompleted;
        private int infrastructureFailed;
        private int safetyCompleted;
        private int safetyFailed;

        @Override
        public List<Long> findPendingInfrastructureBuildingIds(int limit) {
            return List.of();
        }

        @Override
        public List<Long> findPendingSafetyBuildingIds(int limit) {
            return List.of();
        }

        @Override
        public int claimInfrastructure(Long buildingId) {
            if (infrastructureClaimFails) {
                throw new IllegalStateException("claim failed");
            }
            return infrastructureClaimed ? 1 : 0;
        }

        @Override
        public int completeInfrastructure(Long buildingId) {
            infrastructureCompleted++;
            return 1;
        }

        @Override
        public int failInfrastructure(Long buildingId) {
            infrastructureFailed++;
            return 1;
        }

        @Override
        public int claimSafety(Long buildingId) {
            return safetyClaimed ? 1 : 0;
        }

        @Override
        public int completeSafety(Long buildingId) {
            safetyCompleted++;
            return 1;
        }

        @Override
        public int failSafety(Long buildingId) {
            safetyFailed++;
            return 1;
        }
    }

    private static class StubInfrastructureService
            implements BuildingInfraBatchService {

        private final List<Long> processedBuildingIds = new ArrayList<>();
        private boolean fail;

        @Override
        public void processPendingBuildings(int limit) {
        }

        @Override
        public void processSingleBuilding(Long buildingId) {
            processedBuildingIds.add(buildingId);
            if (fail) {
                throw new IllegalStateException("infra failed");
            }
        }
    }

    private static class StubSafetyService implements SafetyAggregationService {

        private final List<Long> processedBuildingIds = new ArrayList<>();

        @Override
        public List<BuildingVO> findPendingBuildings(int limit) {
            return List.of();
        }

        @Override
        public void aggregateBuilding(Long buildingId) {
            processedBuildingIds.add(buildingId);
        }
    }
}
