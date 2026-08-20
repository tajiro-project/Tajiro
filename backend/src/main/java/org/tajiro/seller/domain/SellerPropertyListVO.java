package org.tajiro.seller.domain;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SellerPropertyListVO {
    private Long propertyId;
    private String title;
    private String propertyType;
    private String buildingName;
    private String tradeType;
    private Integer deposit;
    private Integer monthlyRent;
    private BigDecimal areaM2;
    private String floorInfo;
    private Integer favoriteCount;
    private Boolean transactionStatus;
    private String imageUrl;
}
