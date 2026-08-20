package org.tajiro.favorite.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class FavoriteDTO {
    private Long propertyId;
    private String title;
    private String tradeType;
    private Integer deposit;
    private Integer monthlyRent;
    private BigDecimal evaluationScore;
    private String imageUrl;
    private String propertyType;
    private BigDecimal areaM2;
    private String floorInfo;
    private String address;
}
