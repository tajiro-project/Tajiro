package org.tajiro.property.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tajiro.property.dto.PropertyInfrastructureDTO;
import org.tajiro.property.mapper.InfrastructureMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BuildingInfraServiceImpl implements BuildingInfraService {

    private final InfrastructureMapper infrastructureMapper;
    @Override
    @Transactional(readOnly = true)
    public List<PropertyInfrastructureDTO.InfrastructureInfoDTO> getInfraPoints(Long buildingId) {
        return infrastructureMapper.selectInfraPointsByBuildingId(buildingId);
    }
}
