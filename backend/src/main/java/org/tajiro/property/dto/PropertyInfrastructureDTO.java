package org.tajiro.property.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.tajiro.property.domain.BuildingVO;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "건물 주변 인프라 종합 정보 DTO")
public class PropertyInfrastructureDTO {

    @ApiModelProperty(value = "건물 위도")
    private BigDecimal propertyLatitude;

    @ApiModelProperty(value = "건물 경도")
    private BigDecimal propertyLongitude;

    @ApiModelProperty(value = "카테고리별 인프라 목록")
    private List<InfrastructureInfoDTO> infrastructures;

    public static PropertyInfrastructureDTO of(BuildingVO buildingVO, List<InfrastructureInfoDTO> infrastructures) {
        if (buildingVO == null) {
            return null;
        }

        return PropertyInfrastructureDTO.builder()
                .propertyLatitude(buildingVO.getLatitude())
                .propertyLongitude(buildingVO.getLongitude())
                .infrastructures(infrastructures)
                .build();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ApiModel(description = "지도 표시용 인프라 점 DTO")
    public static class InfrastructureInfoDTO {

        @ApiModelProperty(value = "인프라 카테고리 명")
        private String infraCategory;

        @ApiModelProperty(value = "인프라 이름")
        private String infraName;

        @ApiModelProperty(value = "도로명 주소")
        private String roadAddress;

        @ApiModelProperty(value = "위도")
        private BigDecimal latitude;

        @ApiModelProperty(value = "경도")
        private BigDecimal longitude;

        @ApiModelProperty(value = "매물과의 거리(m)")
        private Integer distanceM;

        @ApiModelProperty(value = "정렬 순서")
        private Integer sortOrder;
    }
}