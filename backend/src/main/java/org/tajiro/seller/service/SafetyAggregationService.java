package org.tajiro.seller.service;

import org.tajiro.property.domain.BuildingVO;

import java.util.List;

public interface SafetyAggregationService {

    List<BuildingVO> findPendingBuildings(int limit);

    void aggregateBuilding(Long buildingId);
}