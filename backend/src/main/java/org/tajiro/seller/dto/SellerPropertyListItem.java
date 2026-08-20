package org.tajiro.seller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.tajiro.seller.domain.SellerPropertyListVO;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerPropertyListItem {
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

    public static SellerPropertyListItem from(SellerPropertyListVO vo) {
        return SellerPropertyListItem.builder()
                .propertyId(vo.getPropertyId())
                .title(vo.getTitle())
                .propertyType(vo.getPropertyType())
                .buildingName(vo.getBuildingName())
                .tradeType(vo.getTradeType())
                .deposit(vo.getDeposit())
                .monthlyRent(vo.getMonthlyRent())
                .areaM2(vo.getAreaM2())
                .floorInfo(vo.getFloorInfo())
                .favoriteCount(vo.getFavoriteCount())
                .transactionStatus(vo.getTransactionStatus())
                .imageUrl(vo.getImageUrl())
                .build();
    }
}
