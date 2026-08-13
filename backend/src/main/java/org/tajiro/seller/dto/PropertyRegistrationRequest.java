package org.tajiro.seller.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "매물 등록 요청")
public class PropertyRegistrationRequest {

    @ApiModelProperty(value = "매물 제목")
    private String title;

    @ApiModelProperty(value = "매물 유형", required = true,
            allowableValues = "원룸,아파트,주택/빌라,오피스텔")
    private String propertyType;

    @ApiModelProperty(value = "거래 유형", required = true,
            allowableValues = "월세,전세,매매")
    private String tradeType;

    @ApiModelProperty(value = "보증금·전세금·매매가(만원)", required = true)
    private Integer deposit;

    @ApiModelProperty(value = "월세(만원). 월세가 아니면 0으로 저장")
    private Integer monthlyRent;

    @ApiModelProperty(value = "관리비(만원)")
    private Integer maintenanceFee;

    @ApiModelProperty(value = "면적(m²)", required = true)
    private BigDecimal areaM2;

    @ApiModelProperty(value = "층수 정보", required = true, example = "2 / 4층")
    private String floorInfo;

    @ApiModelProperty(value = "호수", example = "502")
    private String roomNumber;

    @ApiModelProperty(value = "방 수")
    private Integer roomNum;

    @ApiModelProperty(value = "욕실 수")
    private Integer bathroomNum;

    @ApiModelProperty(value = "주차 가능 여부")
    private Boolean parkAvailability;

    @ApiModelProperty(value = "입주일 협의 가능 여부")
    private Boolean discussionStatus;

    @ApiModelProperty(value = "건물 동", example = "101동")
    private String dong;

    @ApiModelProperty(value = "매물 설명")
    private String propertyDescription;

    @ApiModelProperty(value = "입주 가능일")
    private LocalDate moveInDate;

    @ApiModelProperty(value = "사용 승인일", required = true)
    private LocalDate availableDate;

    @ApiModelProperty(value = "주소 및 건물 위치", required = true)
    private LocationRequest location;

    @ApiModelProperty(value = "이미지 URL 목록(최대 10개)")
    private List<String> imageUrls;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ApiModel(description = "매물의 주소 및 건물 위치")
    public static class LocationRequest {

        @ApiModelProperty(value = "도로명 주소", required = true)
        private String address;

        @ApiModelProperty(value = "건물명")
        private String buildingName;

        @ApiModelProperty(value = "위도")
        private BigDecimal lat;

        @ApiModelProperty(value = "경도")
        private BigDecimal lng;
    }
}
