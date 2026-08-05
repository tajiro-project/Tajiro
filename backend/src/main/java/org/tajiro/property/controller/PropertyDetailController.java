package org.tajiro.property.controller;

import io.swagger.annotations.Api;
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
@Api(tags = "상세 매물 내역", description = "매물 상세 내역 확인")
public class PropertyDetailController {

    private final PropertyDetailService propertyDetailService;

    @GetMapping("/{propertyId}")
    @ResponseBody
    public ResponseEntity<ApiResponse<PropertyDetailDTO>> getPropertyDetail(@PathVariable("propertyId") Long id) {
        PropertyDetailDTO dto = propertyDetailService.getPropertyDetail(id);

        return ResponseEntity.ok(ApiResponse.success(dto));
    }
}