package org.tajiro.property.service;

import org.tajiro.property.dto.PropertyInfrastructureDTO.InfrastructureInfoDTO; // 👈 팀원 DTO 경로로 설정

import java.util.List;

public interface BuildingInfraService {

    List<InfrastructureInfoDTO> getInfraPoints(Long buildingId);
}