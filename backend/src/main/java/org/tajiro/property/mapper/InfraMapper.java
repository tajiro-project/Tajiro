package org.tajiro.property.mapper;

import org.apache.ibatis.annotations.Param;
import org.tajiro.property.domain.BuildingVO;
import org.tajiro.property.domain.InfrastructureDetailVO;
import org.tajiro.property.domain.InfrastructureVO;

import java.util.List;

public interface InfraMapper {

    // 인프라 수집 내역이 없는 건물 목록 조회
    List<BuildingVO> selectTargetBuildings(@Param("limit") int limit);

    // 단일 건물 조회
    BuildingVO selectBuildingById(@Param("id") Long id);

    // 인프라 요약 정보 Upsert
    void upsertInfrastructure(InfrastructureVO infra);

    // PK 조회를 위한 단건 검색
    InfrastructureVO selectInfrastructureByBuildingAndCategory(@Param("buildingId") Long buildingId,
                                                               @Param("infraCategory") String infraCategory);

    // 기존 상세 데이터 삭제
    void deleteInfrastructureDetailByInfraId(@Param("infraId") Long infraId);

    // 인프라 상세 단건 Insert
    void insertInfrastructureDetail(InfrastructureDetailVO detail);

    // InfraMapper.java 내 추가
    List<InfrastructureVO> selectInfrastructureByBuildingId(@Param("buildingId") Long buildingId);

    List<InfrastructureDetailVO> selectDetailsByInfraId(@Param("infraId") Long infraId);
}