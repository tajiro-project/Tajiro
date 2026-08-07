package org.tajiro.property.service;

import org.tajiro.property.dto.PropertySafetyDTO;

public interface SafetyService {
    PropertySafetyDTO getPropertySafetyInfo(Long propertyId);
}