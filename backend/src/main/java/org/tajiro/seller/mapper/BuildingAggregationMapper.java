package org.tajiro.seller.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BuildingAggregationMapper {

    List<Long> findPendingInfrastructureBuildingIds(@Param("limit") int limit);

    List<Long> findPendingSafetyBuildingIds(@Param("limit") int limit);

    int claimInfrastructure(@Param("buildingId") Long buildingId);

    int completeInfrastructure(@Param("buildingId") Long buildingId);

    int failInfrastructure(@Param("buildingId") Long buildingId);

    int claimSafety(@Param("buildingId") Long buildingId);

    int completeSafety(@Param("buildingId") Long buildingId);

    int failSafety(@Param("buildingId") Long buildingId);
}
