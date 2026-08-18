package org.tajiro.seller.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyRegistrationVO {
    private Long id;
    private Long sellerId;
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
    private LocalDateTime updateDate;
    private Boolean discussionStatus;
    private String dong;
}
