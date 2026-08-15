package org.tajiro.seller.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.tajiro.common.api.ApiResponse;
import org.tajiro.common.api.ErrorCode;
import org.tajiro.exception.BusinessException;
import org.tajiro.seller.dto.PropertyRegistrationRequest;
import org.tajiro.seller.dto.PropertyRegistrationResponse;
import org.tajiro.seller.dto.PropertyStatusUpdateRequest;
import org.tajiro.seller.dto.SellerPropertyPageResponse;
import org.tajiro.seller.dto.SellerPropertyDetailResponse;
import org.tajiro.seller.service.PropertyRegistrationService;
import org.tajiro.seller.service.SellerPropertyService;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping("/api/seller/properties")
@RequiredArgsConstructor
@Api(tags = "매도자 매물")
public class PropertyRegistrationController {

    private final PropertyRegistrationService propertyRegistrationService;
    private final SellerPropertyService sellerPropertyService;

    @GetMapping("/{propertyId}")
    @ApiOperation(value = "수정 화면용 내 매물 단건 조회")
    public ResponseEntity<ApiResponse<SellerPropertyDetailResponse>> getMyProperty(
            @ApiIgnore @AuthenticationPrincipal Long userId,
            @PathVariable Long propertyId) {
        requireUser(userId);
        return ResponseEntity.ok(ApiResponse.success(
                sellerPropertyService.getProperty(userId, propertyId)));
    }

    @GetMapping
    @ApiOperation(value = "내 매물 목록 조회")
    public ResponseEntity<ApiResponse<SellerPropertyPageResponse>> getMyProperties(
            @ApiIgnore @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "4") int size,
            @RequestParam(defaultValue = "ALL") String status) {
        requireUser(userId);
        return ResponseEntity.ok(ApiResponse.success(
                sellerPropertyService.getProperties(userId, page, size, status)));
    }

    @PatchMapping("/{propertyId}/status")
    @ApiOperation(value = "매물 게시 상태 변경")
    public ResponseEntity<Void> changeStatus(
            @ApiIgnore @AuthenticationPrincipal Long userId,
            @PathVariable Long propertyId,
            @RequestBody PropertyStatusUpdateRequest request) {
        requireUser(userId);
        if (request == null || request.getTransactionStatus() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        sellerPropertyService.changeStatus(
                userId, propertyId, request.getTransactionStatus());
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    @ApiOperation(
            value = "매물 등록",
            notes = "매물을 우선 등록하고 건물 위치 해석 결과에 따라 인프라·안전 집계를 대기시킵니다."
    )
    public ResponseEntity<ApiResponse<PropertyRegistrationResponse>> register(
            @ApiIgnore @AuthenticationPrincipal Long userId,
            @RequestBody PropertyRegistrationRequest request
    ) {
        requireUser(userId);

        PropertyRegistrationResponse response =
                propertyRegistrationService.register(userId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    private void requireUser(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.AUTH_REQUIRED);
        }
    }
}
