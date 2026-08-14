package org.tajiro.seller.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "매물 등록 응답")
public class PropertyRegistrationResponse {

    @ApiModelProperty(value = "등록된 매물 ID")
    private Long propertyId;

    @ApiModelProperty(value = "연결된 건물 ID. 위치 해석 대기 중이면 null")
    private Long buildingId;

    @ApiModelProperty(value = "건물 위치 처리 상태", allowableValues = "RESOLVED,PENDING")
    private String locationStatus;

    @ApiModelProperty(value = "인프라·안전 집계 상태",
            allowableValues = "PENDING,WAITING_FOR_LOCATION")
    private String aggregationStatus;
}
