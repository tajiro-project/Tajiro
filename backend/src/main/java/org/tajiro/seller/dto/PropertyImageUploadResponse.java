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
@ApiModel(description = "매물 이미지 업로드 응답")
public class PropertyImageUploadResponse {

    @ApiModelProperty(value = "저장소 객체 키")
    private String objectKey;

    @ApiModelProperty(value = "매물 등록 요청에 사용할 이미지 URL")
    private String imageUrl;

    @ApiModelProperty(value = "이미지 MIME 타입")
    private String contentType;

    @ApiModelProperty(value = "이미지 크기(byte)")
    private Long size;
}
