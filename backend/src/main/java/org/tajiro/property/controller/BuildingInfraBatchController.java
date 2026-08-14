package org.tajiro.property.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tajiro.common.api.ApiResponse;
import org.tajiro.property.service.BuildingInfraBatchService;

@RestController
@RequestMapping("/api/admin/infra/batch")
@RequiredArgsConstructor
@Api(tags = "인프라 수집 배치", description = "관리자용 인프라 수집 컨트롤러")
public class BuildingInfraBatchController {

    private final BuildingInfraBatchService buildingInfraBatchService;

    @PostMapping("/building/{buildingId}")
    @ApiOperation(value = "단일 건물 인프라 수집", notes = "특정 건물 반경 2km 내의 주변 인프라를 즉시 수집합니다.")
    public ResponseEntity<ApiResponse<String>> processSingleBuilding(@PathVariable Long buildingId) {
        buildingInfraBatchService.processSingleBuilding(buildingId);
        return ResponseEntity.ok(ApiResponse.success("Building ID " + buildingId + " infra collection completed."));
    }

    @PostMapping("/pending")
    @ApiOperation(value = "미수집 건물 배치 수집", notes = "인프라 내역이 없는 건물을 지정한 개수만큼 배치 수집합니다.")
    public ResponseEntity<ApiResponse<String>> processPendingBuildings(@RequestParam(defaultValue = "10") int limit) {
        buildingInfraBatchService.processPendingBuildings(limit);
        return ResponseEntity.ok(ApiResponse.success("Batch processing started for top " + limit + " pending buildings."));
    }
}