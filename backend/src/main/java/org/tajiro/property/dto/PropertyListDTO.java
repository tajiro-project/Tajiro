package org.tajiro.property.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.tajiro.property.domain.PropertyVO;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyListDTO {
    private Long id;
    private Long buildingId;
    private String buildingName;
    private String title;
    private String propertyType;
    private String tradeType;
    private Integer deposit;
    private Integer monthlyRent;
    private Integer maintenanceFee;
    private BigDecimal areaM2;
    private String floorInfo;
    private String address;
    private String thumbnailUrl;
    private Boolean transactionStatus;

    private BigDecimal latitude;
    private BigDecimal longitude;

    private Integer distanceMeters;
    private Integer recommendScore;
    private Integer desiredInfraCount;
    private Integer desiredAmenityCount;

    public static PropertyListDTO of(PropertyVO property) {
        return PropertyListDTO.builder()
                .id(property.getId())
                .buildingId(property.getBuildingId())
                .buildingName(property.getBuildingVO().getBldNm())
                .title(property.getTitle())
                .propertyType(property.getPropertyType())
                .tradeType(property.getTradeType())
                .deposit(property.getDeposit())
                .monthlyRent(property.getMonthlyRent())
                .maintenanceFee(property.getMaintenanceFee())
                .areaM2(property.getAreaM2())
                .floorInfo(property.getFloorInfo())
                .address(property.getAddress())
                .thumbnailUrl(property.getThumbnailUrl())
                .transactionStatus(property.getTransactionStatus())
                .latitude(property.getBuildingVO().getLatitude())
                .longitude(property.getBuildingVO().getLongitude())
                .distanceMeters(property.getDistanceMeters())
                .recommendScore(property.getPropertyValueAnalysisResultVO() == null
                        ? null
                        : property.getPropertyValueAnalysisResultVO().getRecommendScore())
                .desiredInfraCount(property.getDesiredInfraCount())
                .desiredAmenityCount(property.getDesiredAmenityCount())
                .build();
    }
}
