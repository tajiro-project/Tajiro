package org.tajiro.seller.service;

import org.tajiro.seller.dto.SellerPropertyPageResponse;
import org.tajiro.seller.dto.SellerPropertyDetailResponse;

public interface SellerPropertyService {
    SellerPropertyDetailResponse getProperty(Long sellerId, Long propertyId);

    SellerPropertyPageResponse getProperties(
            Long sellerId, int page, int size, String status);

    void changeStatus(Long sellerId, Long propertyId, boolean transactionStatus);
}
