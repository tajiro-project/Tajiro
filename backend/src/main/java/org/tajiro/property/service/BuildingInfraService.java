package org.tajiro.property.service;

import org.tajiro.property.dto.PropertyInfrastructureDTO;

import java.util.List;

public interface BuildingInfraService {
    List<PropertyInfrastructureDTO.InfrastructureInfoDTO> getInfraPoints(Long buildingId);
}