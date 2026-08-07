package org.tajiro.property.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.tajiro.property.dto.PropertySafetyDTO;

@Mapper
public interface SafetyMapper {
    boolean existsPropertyById(@Param("propertyId") Long propertyId);

    PropertySafetyDTO selectPropertySafetyInfo(@Param("propertyId") Long propertyId);
}