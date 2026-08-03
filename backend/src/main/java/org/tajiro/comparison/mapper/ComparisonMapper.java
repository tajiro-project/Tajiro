package org.tajiro.comparison.mapper;

import org.apache.ibatis.annotations.Param;
import org.tajiro.comparison.dto.ComparePropertyDTO;

import java.util.List;

public interface ComparisonMapper {
    //사용자의 비교함 목록 조회 (사용자 ID를 입력받아, 해당 사용자의 비교함 매물 목록을 ComparePropertyDTO 리스트 형태로 반환)
    List<ComparePropertyDTO> findByUserId(@Param("userId") Long userId);
    //매물 존재 여부 조회
    boolean existsProperty(@Param("propertyId") Long propertyId);
    //사용자의 비교함에 매물이 존재하는지 여부 조회
    boolean existsByUserIdAndPropertyId(@Param("userId") Long userId,
                                        @Param("propertyId") Long propertyId);
    //사용자의 비교함에 담긴 매물 수 조회
    int countByUserId(@Param("userId") Long userId);
    //비교함에 매물 추가
    int insert(@Param("userId") Long userId, @Param("propertyId") Long propertyId);
    //비교함에서 매물 제거
    int delete(@Param("userId") Long userId, @Param("propertyId") Long propertyId);
}
