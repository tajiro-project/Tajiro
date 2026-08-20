package org.tajiro.property.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.tajiro.property.domain.InfrastructureDetailVO;
import org.tajiro.property.domain.InfrastructureVO;
import org.tajiro.property.dto.PropertyInfrastructureDTO.InfrastructureInfoDTO;
import org.tajiro.property.mapper.InfraMapper;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BuildingInfraServiceImpl implements BuildingInfraService {

    private final InfraMapper infraMapper;

    @Override
    public List<InfrastructureInfoDTO> getInfraPoints(Long buildingId) {
        // 1. 건물 ID로 수집된 인프라 카테고리 목록 조회
        List<InfrastructureVO> infraList = infraMapper.selectInfrastructureByBuildingId(buildingId);
        List<InfrastructureInfoDTO> result = new ArrayList<>();

        for (InfrastructureVO infra : infraList) {
            // 2. 카테고리별 상위 상세지점 목록 조회 (가까운 순 3개)
            List<InfrastructureDetailVO> details = infraMapper.selectDetailsByInfraId(infra.getId());

            // 3. 지도에 핀을 찍을 수 있도록 평탄하게(Flat) DTO 목록 생성
            for (InfrastructureDetailVO detail : details) {
                InfrastructureInfoDTO dto = InfrastructureInfoDTO.builder()
                        .infraCategory(infra.getInfraCategory())
                        .infraName(detail.getInfraName())
                        .roadAddress(detail.getRoadAddress())
                        .latitude(detail.getLatitude())
                        .longitude(detail.getLongitude())
                        .distanceM(detail.getDistanceM())
                        .sortOrder(detail.getSortOrder())
                        .build();

                result.add(dto);
            }
        }

        return result;
    }
}