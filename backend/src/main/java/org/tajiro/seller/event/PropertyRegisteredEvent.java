package org.tajiro.seller.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PropertyRegisteredEvent {

    private final Long propertyId;
    private final Long buildingId;
}
