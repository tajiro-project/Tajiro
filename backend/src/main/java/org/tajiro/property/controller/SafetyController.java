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
import org.tajiro.property.dto.PropertySafetyDTO;
import org.tajiro.property.service.SafetyService;

@Controller
@RequestMapping("/api/properties")
@RequiredArgsConstructor
@Api(tags = "매물 상세", description = "매물 상세 관련 API")
public class SafetyController {

    private final SafetyService safetyService;

    @GetMapping("/{propertyId}/safety")
    @ResponseBody
    @ApiOperation(value = "매물 안전 정보 조회", notes = "매물 주변의 안전 인프라(지구대, CCTV, 비상벨 등) 집계 및 상세 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<PropertySafetyDTO>> getPropertySafetyInfo(@PathVariable("propertyId") Long propertyId) {
        PropertySafetyDTO dto = safetyService.getPropertySafetyInfo(propertyId);

        return ResponseEntity.ok(ApiResponse.success(dto));
    }
}