package org.tajiro.property.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tajiro.common.api.ApiResponse;
import org.tajiro.common.api.ErrorCode;
import org.tajiro.exception.BusinessException;
import org.tajiro.property.dto.PropertyListDTO;
import org.tajiro.property.service.PropertyService;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

@RestController
@RequestMapping("/api/properties")
@RequiredArgsConstructor
@Api(tags = "매물")
public class PropertyController {
    private final PropertyService propertyService;

    @GetMapping
    @ApiOperation(
            value = "매물 목록 조회",
            notes = "저장된 주거 선호 조건으로 조회하며, 조건이 없으면 404 를 준다.")
    public ResponseEntity<ApiResponse<List<PropertyListDTO>>> getList(
            @ApiIgnore @AuthenticationPrincipal Long userId) {
        requireUserId(userId);

        return ResponseEntity.ok(ApiResponse.success(propertyService.getList(userId)));
    }

    private Long requireUserId(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.AUTH_REQUIRED);
        }
        return userId;
    }
}
