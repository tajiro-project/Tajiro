package org.tajiro.property.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.tajiro.property.domain.BuildingVO;
import org.tajiro.property.dto.PropertyInfrastructureDTO;
import org.tajiro.property.dto.PropertyInfrastructureDTO.InfrastructureInfoDTO;

import java.util.List;

@Mapper
public interface InfrastructureMapper {

    Long selectBuildingIdByPropertyId(@Param("propertyId") Long propertyId);

    BuildingVO selectBuildingById(@Param("buildingId") Long buildingId);

    List<InfrastructureInfoDTO> selectNearestInfrastructuresByBuildingId(@Param("buildingId") Long buildingId);

    List<PropertyInfrastructureDTO.InfrastructureInfoDTO> selectInfraPointsByBuildingId(@Param("buildingId") Long buildingId);
}