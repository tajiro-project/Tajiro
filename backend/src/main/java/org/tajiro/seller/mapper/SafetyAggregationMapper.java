package org.tajiro.seller.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.tajiro.property.domain.BuildingVO;
import org.tajiro.seller.dto.SafetyAggregationDTO;
import org.tajiro.seller.dto.SafetyPoiDTO;

import java.util.List;

@Mapper
public interface SafetyAggregationMapper {

    List<BuildingVO> findPendingBuildings(
            @Param("categories") List<String> categories,
            @Param("categoryCount") int categoryCount,
            @Param("limit") int limit
    );

    BuildingVO findBuildingById(
            @Param("buildingId") Long buildingId
    );

    List<SafetyAggregationDTO> aggregateSafety(
            @Param("categories") List<String> categories,
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("dlat") double dlat,
            @Param("dlng") double dlng,
            @Param("radius") int radius
    );

    int upsertSafety(
            @Param("buildingId") Long buildingId,
            @Param("safeCategory") String safeCategory,
            @Param("countWithin500m") int countWithin500m,
            @Param("nearestDistanceMeters") Integer nearestDistanceMeters
    );

    Long findSafetyId(
            @Param("buildingId") Long buildingId,
            @Param("safeCategory") String safeCategory
    );

    int deleteSafetyDetails(
            @Param("safeId") Long safeId
    );

    List<SafetyPoiDTO> findSafetyPois(
            @Param("category") String category,
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("dlat") double dlat,
            @Param("dlng") double dlng,
            @Param("radius") int radius
    );

    int insertSafetyDetails(
            @Param("details") List<SafetyPoiDTO> details
    );
}