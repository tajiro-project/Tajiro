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

    // 2. 추천 점수 조회 (✨ 누락된 메서드 추가)
    Integer selectRecommendScore(@Param("propertyId") Long propertyId, @Param("userId") Long userId);

    // 3. 매물 이미지 URL 목록 조회
    List<String> selectPropertyImages(@Param("id") Long id);

    // 4. 건물 관리 번호(buildingId) 기준 인프라 요약 목록 조회
    List<PropertyDetailDTO.InfraSummaryDTO> selectInfraSummaryByBuildingId(@Param("buildingId") Long buildingId);

    // 5. 특정 유저가 해당 매물을 찜했는지 확인하는 쿼리 메서드
    boolean selectIsFavorite(@Param("userId") Long userId, @Param("propertyId") Long propertyId);
}