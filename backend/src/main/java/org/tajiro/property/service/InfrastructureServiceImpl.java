package org.tajiro.property.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.tajiro.common.api.ErrorCode;
import org.tajiro.exception.BusinessException;
import org.tajiro.property.domain.BuildingVO;
import org.tajiro.property.dto.PropertyInfrastructureDTO;
import org.tajiro.property.dto.PropertyInfrastructureDTO.InfrastructureInfoDTO;
import org.tajiro.property.mapper.InfrastructureMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InfrastructureServiceImpl implements InfrastructureService {

    private final InfrastructureMapper infrastructureMapper;

    @Override
    public PropertyInfrastructureDTO getPropertyInfrastructures(Long propertyId) {
        // 1. 매물 존재 여부 확인 및 building_id 조회
        Long buildingId = infrastructureMapper.selectBuildingIdByPropertyId(propertyId);
        if (buildingId == null) {
            throw new BusinessException(ErrorCode.PROPERTY_NOT_FOUND);
        }

        // 2. BuildingVO 조회 (건물 위/경도 정보 포함)
        BuildingVO buildingVO = infrastructureMapper.selectBuildingById(buildingId);
        if (buildingVO == null) {
            throw new BusinessException(ErrorCode.PROPERTY_NOT_FOUND);
        }

        // 3. building_id 기반 카테고리별 최단거리 인프라(sort_order = 1) 목록 조회
        List<InfrastructureInfoDTO> nearestInfra =
                infrastructureMapper.selectNearestInfrastructuresByBuildingId(buildingId);

        if (nearestInfra == null || nearestInfra.isEmpty()) {
            throw new BusinessException(ErrorCode.INFRASTRUCTURE_NOT_FOUND);
        }

        // 4. BuildingVO와 인프라 목록을 조합하여 응답 DTO 생성 후 반환
        return PropertyInfrastructureDTO.of(buildingVO, nearestInfra);
    }
}