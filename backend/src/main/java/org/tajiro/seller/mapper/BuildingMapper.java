package org.tajiro.seller.mapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.tajiro.seller.domain.BuildingCreateVO;

import java.math.BigDecimal;

@Mapper
public interface BuildingMapper {
    Long findIdByBuildingCode(@Param("buildingCode") String buildingCode);
    Long findNearbyId(
            @Param("latitude") BigDecimal latitude,
            @Param("longitude") BigDecimal longitude,
            @Param("radiusMeters") int radiusMeters
    );
    void insertBuilding(BuildingCreateVO buildingCreateVO);
}