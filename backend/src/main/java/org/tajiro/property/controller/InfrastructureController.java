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
import org.tajiro.property.dto.PropertyInfrastructureDTO;
import org.tajiro.property.service.InfrastructureService;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping("/api/properties")
@RequiredArgsConstructor
@Api(tags = "06. 매물 탐색", description = "매물 주변 생활 인프라 조회 API")
public class InfrastructureController {

    private final InfrastructureService infrastructureService;

    // 팀 공통 인증 검증 메서드
    private Long requireUserId(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.AUTH_REQUIRED);
        }
        return userId;
    }

    @GetMapping("/{propertyId}/infrastructures")
    @ApiOperation(value = "매물 주변 생활 인프라 조회", notes = "매물 ID를 받아 카테고리별 가장 가까운 인프라 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<PropertyInfrastructureDTO>> getPropertyInfrastructures(
            @ApiIgnore @AuthenticationPrincipal Long userId,
            @ApiParam(value = "매물 ID", example = "72") @PathVariable("propertyId") Long propertyId
    ) {
        // 1. 로그인 필수 검증 (비로그인이면 AUTH_REQUIRED 예외 발생)
        requireUserId(userId);

        // 2. Service 호출
        PropertyInfrastructureDTO dto = infrastructureService.getPropertyInfrastructures(propertyId);

        return ResponseEntity.ok(ApiResponse.success(dto));
    }
}
