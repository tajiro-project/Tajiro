package org.tajiro.location.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel(description = "주소 검색 결과")
public class AddressResolveResult {
    @ApiModelProperty(value = "도로명 주소", example = "대전 동구 백룡로57번길 126")
    private String roadAddress;

    @ApiModelProperty(value = "지번 주소", example = "대전 동구 자양동 1-1")
    private String jibunAddress;

    @ApiModelProperty(value = "건물명", example = "우송에이스빌")
    private String buildingName;

    @ApiModelProperty(value = "법정동명", example = "자양동")
    private String dongName;

    @ApiModelProperty(value = "위도", example = "36.3387162")
    private BigDecimal lat;

    @ApiModelProperty(value = "경도", example = "127.4507735")
    private BigDecimal lng;

    @ApiModelProperty(value = "아파트 여부. 건축물대장 연동 전까지 항상 false")
    private boolean apartment;

    @ApiModelProperty(value = "동 목록. 건축물대장 연동 전까지 빈 배열")
    private List<String> dongs;

    @ApiModelProperty(value = "필지고유번호 19자리", example = "3011012000100010001")
    private String pnu;

    @ApiModelProperty(value = "시군구코드 5자리", example = "30110")
    private String sigunguCd;

    @ApiModelProperty(value = "법정동코드 5자리", example = "12000")
    private String bjdongCd;

    @ApiModelProperty(value = "대지구분. 0 대지 / 1 산")
    private String platGbCd;

    @ApiModelProperty(value = "본번 4자리")
    private String bun;

    @ApiModelProperty(value = "부번 4자리")
    private String ji;

    public static AddressResolveResult.AddressResolveResultBuilder base() {
        return AddressResolveResult.builder()
                .apartment(false)
                .dongs(Collections.emptyList());
    }
}
