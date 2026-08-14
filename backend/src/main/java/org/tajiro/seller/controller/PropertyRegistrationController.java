package org.tajiro.seller.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tajiro.common.api.ApiResponse;
import org.tajiro.common.api.ErrorCode;
import org.tajiro.exception.BusinessException;
import org.tajiro.seller.dto.PropertyRegistrationRequest;
import org.tajiro.seller.dto.PropertyRegistrationResponse;
import org.tajiro.seller.service.PropertyRegistrationService;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping("/api/seller/properties")
@RequiredArgsConstructor
@Api(tags = "판매자 매물")
public class PropertyRegistrationController {

    private final PropertyRegistrationService propertyRegistrationService;

    @PostMapping
    @ApiOperation(
            value = "매물 등록",
            notes = "매물을 우선 등록하고 건물 위치 해석 결과에 따라 인프라·안전 집계를 대기시킵니다."
    )
    public ResponseEntity<ApiResponse<PropertyRegistrationResponse>> register(
            @ApiIgnore @AuthenticationPrincipal Long userId,
            @RequestBody PropertyRegistrationRequest request
    ) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.AUTH_REQUIRED);
        }

        PropertyRegistrationResponse response =
                propertyRegistrationService.register(userId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }
}
