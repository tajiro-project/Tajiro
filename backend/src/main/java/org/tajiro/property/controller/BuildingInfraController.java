package org.tajiro.property.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tajiro.common.api.ApiResponse;
import org.tajiro.common.api.ErrorCode;
import org.tajiro.exception.BusinessException;
import org.tajiro.property.dto.PropertyInfrastructureDTO.InfrastructureInfoDTO;
import org.tajiro.property.service.BuildingInfraService;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

@RestController
@RequestMapping("/api/buildings")
@RequiredArgsConstructor
@Api(tags = "06. 매물 탐색", description = "건물 주변 인프라 지도 정보 조회 API")
public class BuildingInfraController {

    private final BuildingInfraService buildingInfraService;

    @GetMapping("/{buildingId}/infrastructures")
    @ApiOperation(
            value = "건물 주변 인프라 지점 조회",
            notes = "지도에 표시할 카테고리별 인프라 지점 목록을 반환합니다."
    )
    public ResponseEntity<ApiResponse<List<InfrastructureInfoDTO>>> getInfraPoints(
            @ApiIgnore @AuthenticationPrincipal Long userId,
            @ApiParam(value = "건물 ID", example = "36") @PathVariable("buildingId") Long buildingId
    ) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.AUTH_REQUIRED);
        }

        return ResponseEntity.ok(
                ApiResponse.success(buildingInfraService.getInfraPoints(buildingId))
        );
    }
}
