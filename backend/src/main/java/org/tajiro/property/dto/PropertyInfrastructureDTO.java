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

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@ApiModel(description = "매물 주변 생활 인프라 응답 DTO")
public class PropertyInfrastructureDTO {

    @ApiModelProperty(value = "해당 매물(건물) 위도")
    private BigDecimal propertyLatitude;

    @ApiModelProperty(value = "해당 매물(건물) 경도")
    private BigDecimal propertyLongitude;

    @ApiModelProperty(value = "카테고리별 최단거리 인프라 목록")
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

    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Data
    @ApiModel(description = "카테고리별 최단거리 인프라 정보")
    public static class InfrastructureInfoDTO {

        @ApiModelProperty(value = "인프라 카테고리 명")
        private String infraCategory;

        @ApiModelProperty(value = "인프라 이름")
        private String infraName;

        @ApiModelProperty(value = "매물간의 거리(m)")
        private Integer distanceM;

        @ApiModelProperty(value = "도보 시간(분)")
        private Integer walkMinutes;

        @ApiModelProperty(value = "인프라 위도")
        private BigDecimal latitude;

        @ApiModelProperty(value = "인프라 경도")
        private BigDecimal longitude;
    }
}