package org.tajiro.property.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.tajiro.common.api.ApiResponse;
import org.tajiro.common.api.ErrorCode;
import org.tajiro.exception.BusinessException;
import org.tajiro.property.dto.PropertyListDTO;
import org.tajiro.property.service.PropertyService;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/properties")
@RequiredArgsConstructor
public class PropertyController {
    private final PropertyService propertyService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PropertyListDTO>>> getList(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false)BigDecimal centerLat,
            @RequestParam(required = false)BigDecimal centerLng
            ) {
        requireUserId(userId);

        if((centerLat == null) != (centerLng == null)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        return ResponseEntity.ok(ApiResponse.success(propertyService.getList(userId, centerLat, centerLng)));
    }

    private Long requireUserId(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.AUTH_REQUIRED);
        }
        return userId;
    }
}
