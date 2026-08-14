package org.tajiro.property.service;

public interface BuildingInfraBatchService {

    void processPendingBuildings(int limit);

    void processSingleBuilding(Long buildingId);
}