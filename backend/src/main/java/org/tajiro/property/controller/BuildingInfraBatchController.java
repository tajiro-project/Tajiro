package org.tajiro.property.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tajiro.common.api.ApiResponse;
import org.tajiro.seller.service.PropertyAggregationCoordinator;

@RestController
@RequestMapping("/api/admin/infra/batch")
@RequiredArgsConstructor
@Api(tags = "인프라 수집 배치", description = "관리자용 인프라 수집 컨트롤러")
public class BuildingInfraBatchController {

    private final PropertyAggregationCoordinator aggregationCoordinator;

    @PostMapping("/building/{buildingId}")
    @ApiOperation(value = "단일 건물 인프라 수집", notes = "특정 건물 반경 2km 내의 주변 인프라를 즉시 수집합니다.")
    public ResponseEntity<ApiResponse<String>> processSingleBuilding(@PathVariable Long buildingId) {
        boolean processed = aggregationCoordinator.aggregateInfrastructure(null, buildingId);
        String result = processed ? "completed" : "skipped";
        return ResponseEntity.ok(ApiResponse.success(
                "Building ID " + buildingId + " infra collection " + result + "."));
    }

    @PostMapping("/pending")
    @ApiOperation(value = "대기 건물 배치 수집", notes = "인프라 집계 상태가 PENDING인 건물을 지정한 개수만큼 처리합니다.")
    public ResponseEntity<ApiResponse<String>> processPendingBuildings(@RequestParam(defaultValue = "10") int limit) {
        aggregationCoordinator.processPendingInfrastructure(limit);
        return ResponseEntity.ok(ApiResponse.success("Batch processing started for top " + limit + " pending buildings."));
    }
}
