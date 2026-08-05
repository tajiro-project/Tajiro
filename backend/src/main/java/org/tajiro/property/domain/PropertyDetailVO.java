package org.tajiro.property.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.lang.Long;


@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class PropertyDetailVO {
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
    private LocalDateTime moveInDate;
    private LocalDateTime availableDate;
    private LocalDateTime updateDate;
    private Boolean discussionStatus;
    private String dong;
    private Integer evaluationScore;
}