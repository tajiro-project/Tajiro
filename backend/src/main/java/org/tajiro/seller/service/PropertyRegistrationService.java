package org.tajiro.seller.service;

import org.tajiro.seller.dto.PropertyRegistrationRequest;
import org.tajiro.seller.dto.PropertyRegistrationResponse;

public interface PropertyRegistrationService {

    PropertyRegistrationResponse register(
            Long sellerId,
            PropertyRegistrationRequest request
    );
}
