package org.tajiro.property.service;

import org.tajiro.property.domain.PropertyVO;
import org.tajiro.property.dto.PropertyListDTO;

import java.math.BigDecimal;
import java.util.List;

public interface PropertyService {
    List<PropertyVO> findMatchingProperties(Long userId);

    List<PropertyListDTO> getList(Long userId);
}
