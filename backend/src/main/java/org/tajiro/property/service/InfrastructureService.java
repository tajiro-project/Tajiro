package org.tajiro.property.service;

import org.tajiro.property.dto.PropertyInfrastructureDTO;

public interface InfrastructureService {
    PropertyInfrastructureDTO getPropertyInfrastructures(Long propertyId);
}