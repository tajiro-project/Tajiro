package org.tajiro.property.service;

import org.tajiro.property.domain.PropertyVO;

import java.util.List;

public interface PropertyService {
    List<PropertyVO> findMatchingProperties(Long userId);
}
