package org.tajiro.property.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tajiro.common.api.ApiResponse;
import org.tajiro.property.dto.PropertyInfrastructureDTO.InfrastructureInfoDTO;
import org.tajiro.property.service.BuildingInfraService;

import java.util.List;

@RestController
@RequestMapping("/api/buildings")
@RequiredArgsConstructor
@Api(tags = "건물", description = "건물 단위 조회 API")
public class BuildingInfraController {

    private final BuildingInfraService buildingInfraService;
    @GetMapping("/{buildingId}/infrastructures")
    @ApiOperation(
            value = "건물 주변 인프라 점 조회",
            notes = "지도에 표시할 용도로 카테고리별 가까운 순 3개까지 좌표와 함께 반환합니다.")
    public ResponseEntity<ApiResponse<List<InfrastructureInfoDTO>>> getInfraPoints(
            @ApiParam(value = "buildingId", example = "17")
            @PathVariable("buildingId") Long buildingId) {
        return ResponseEntity.ok(ApiResponse.success(
                buildingInfraService.getInfraPoints(buildingId)));
    }
}