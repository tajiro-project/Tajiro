package org.tajiro.property.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.tajiro.common.api.ApiResponse;
import org.tajiro.common.api.ErrorCode;
import org.tajiro.exception.BusinessException;
import org.tajiro.property.dto.PropertyDetailDTO;
import org.tajiro.property.service.PropertyDetailService;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping("/api/properties")
@RequiredArgsConstructor
@Api(tags = "매물")
public class PropertyDetailController {

    private final PropertyDetailService propertyDetailService;

    // 팀 공통 인증 검증 메서드
    private Long requireUserId(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.AUTH_REQUIRED);
        }
        return userId;
    }

    @GetMapping("/{propertyId}")
    @ApiOperation(value = "매물 상세 정보 조회", notes = "매물 ID에 해당하는 상세 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<PropertyDetailDTO>> getPropertyDetail(
            @ApiIgnore @AuthenticationPrincipal Long userId,
            @ApiParam(value = "매물 ID", example = "158") @PathVariable("propertyId") Long propertyId
    ) {
        // 1. 로그인 필수 검증 (비로그인이면 AUTH_REQUIRED 예외 발생)
        requireUserId(userId);

        // 2. Service 호출 (userId를 전달하여 찜 여부까지 판단)
        PropertyDetailDTO result = propertyDetailService.getPropertyDetail(propertyId, userId);

        return ResponseEntity.ok(ApiResponse.success(result));
    }
}