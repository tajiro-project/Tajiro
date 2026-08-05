package org.tajiro.property.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.tajiro.common.api.ApiResponse;
import org.tajiro.property.dto.PropertyInfrastructureDTO;
import org.tajiro.property.service.InfrastructureService;

@Controller
@RequestMapping("/api/properties")
@RequiredArgsConstructor
@Api(tags = "매물 상세", description = "매물 상세 관련 API")
public class InfrastructureController {

    private final InfrastructureService infrastructureService;

    @GetMapping("/{propertyId}/infrastructures")
    @ResponseBody
    @ApiOperation(value = "매물 주변 생활 인프라 조회", notes = "매물 ID를 받아 카테고리별 가장 가까운 인프라 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<PropertyInfrastructureDTO>> getPropertyInfrastructures(@PathVariable("propertyId") Long propertyId) {
        PropertyInfrastructureDTO dto = infrastructureService.getPropertyInfrastructures(propertyId);

        return ResponseEntity.ok(ApiResponse.success(dto));
    }
}