package org.tajiro.seller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.tajiro.property.domain.BuildingVO;
import org.tajiro.property.domain.PropertyVO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerPropertyDetailResponse {
    private Long propertyId;
    private String title;
    private String propertyType;
    private String tradeType;
    private Integer deposit;
    private Integer monthlyRent;
    private Integer maintenanceFee;
    private BigDecimal areaM2;
    private String floorInfo;
    private String roomNumber;
    private Integer roomNum;
    private Integer bathroomNum;
    private Boolean parkAvailability;
    private Boolean discussionStatus;
    private String dong;
    private String propertyDescription;
    private LocalDate moveInDate;
    private LocalDate availableDate;
    private String address;
    private String roadAddress;
    private String jibunAddress;
    private String buildingName;
    private BigDecimal lat;
    private BigDecimal lng;
    private List<String> imageUrls;

    public static SellerPropertyDetailResponse from(
            PropertyVO vo, List<String> imageUrls) {
        BuildingVO building = vo.getBuildingVO();
        return SellerPropertyDetailResponse.builder()
                .propertyId(vo.getId())
                .title(vo.getTitle())
                .propertyType(vo.getPropertyType())
                .tradeType(vo.getTradeType())
                .deposit(vo.getDeposit())
                .monthlyRent(vo.getMonthlyRent())
                .maintenanceFee(vo.getMaintenanceFee())
                .areaM2(vo.getAreaM2())
                .floorInfo(vo.getFloorInfo())
                .roomNumber(vo.getRoomNumber())
                .roomNum(vo.getRoomNum())
                .bathroomNum(vo.getBathroomNum())
                .parkAvailability(vo.getParkAvailability())
                .discussionStatus(vo.getDiscussionStatus())
                .dong(vo.getDong())
                .propertyDescription(vo.getPropertyDescription())
                .moveInDate(vo.getMoveInDate())
                .availableDate(vo.getAvailableDate())
                .address(vo.getAddress())
                .roadAddress(building == null ? null : building.getRoadAddress())
                .jibunAddress(building == null ? null : building.getJibunAddress())
                .buildingName(building == null ? null : building.getBldNm())
                .lat(building == null ? null : building.getLatitude())
                .lng(building == null ? null : building.getLongitude())
                .imageUrls(imageUrls)
                .build();
    }
}
