package org.tajiro.property.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.tajiro.property.domain.PropertyDetailVO;
import org.tajiro.property.dto.PropertyDetailDTO;

import java.util.List;

@Mapper
public interface PropertyDetailMapper {
    // 1. 매물 상세 정보 조회
    PropertyDetailVO selectPropertyDetail(@Param("id") Long id);

    // 2. 매물 이미지 URL 목록 조회
    List<String> selectPropertyImages(@Param("id") Long id);

    // 3. 건물 관리 번호(buildingId) 기준 인프라 요약 목록 조회
    List<PropertyDetailDTO.InfraSummaryDTO> selectInfraSummaryByBuildingId(@Param("buildingId") Long buildingId);
}