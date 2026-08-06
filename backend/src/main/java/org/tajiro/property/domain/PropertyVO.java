package org.tajiro.property.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyVO {
    private Long id;
    private String sourceListingId;
    private Long buildingId;
    private String title;
    private String propertyType;
    private String tradeType;
    private Integer deposit;
    private Integer monthlyRent;
    private Integer maintenanceFee;
    private BigDecimal areaM2;
    private String floorInfo;
    private String roomNumber;
    private String address;
    private Integer roomNum;
    private Integer bathroomNum;
    private Boolean parkAvailability;
    private Boolean transactionStatus;
    private String propertyDescription;
    private LocalDate availableDate;
    private LocalDate moveInDate;
    private LocalDate updateDate;
    private Boolean discussionStatus;
    private String dong;
    private Integer evaluationScore;

    private Integer distanceMeters;
    private Integer desiredInfraCount;
    private Integer desiredAmenityCount;

    // property_image
    private String thumbnailUrl;

    // building
    private BuildingVO buildingVO;

    // property_value_analysis_result
    private PropertyValueAnalysisResultVO propertyValueAnalysisResultVO;
}
