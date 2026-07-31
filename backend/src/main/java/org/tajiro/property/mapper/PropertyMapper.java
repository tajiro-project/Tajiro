package org.tajiro.property.mapper;

import org.tajiro.property.domain.PropertyVO;
import org.tajiro.property.dto.PropertySearchRequest;

import java.util.List;

public interface PropertyMapper {
    List<PropertyVO> getList(PropertySearchRequest request);
}
