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
import org.tajiro.common.api.ErrorCode;
import org.tajiro.property.dto.PropertyDetailDTO;
import org.tajiro.property.service.PropertyDetailService;

@Controller
@RequestMapping("/api/properties")
@RequiredArgsConstructor
@Api(tags = "매물 상세", description = "매물 상세 관련 API")
public class PropertyDetailController {

    private final PropertyDetailService propertyDetailService;

    @GetMapping("/{propertyId}")
    @ResponseBody
    @ApiOperation(value = "매물 상세 내역 조회", notes = "매물 상세 내역을 확인합니다.")
    public ResponseEntity<ApiResponse<PropertyDetailDTO>> getPropertyDetail(@PathVariable("propertyId") Long id) {
        PropertyDetailDTO dto = propertyDetailService.getPropertyDetail(id);

        return ResponseEntity.ok(ApiResponse.success(dto));
    }
}