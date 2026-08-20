package org.tajiro.preference.controller;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tajiro.common.api.ApiResponse;
import org.tajiro.common.api.ErrorCode;
import org.tajiro.exception.BusinessException;
import org.tajiro.preference.dto.PreferenceDTO;
import org.tajiro.preference.service.PreferenceService;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping("/api/users/me/preferences")
@RequiredArgsConstructor
@Api(tags = "04. 주거 선호 조건", description = "사용자의 주거 선호 조건 조회 및 저장 API")
public class PreferenceController {

    private final PreferenceService preferenceService;

    @GetMapping
    public ResponseEntity<ApiResponse<PreferenceDTO>> get(
            @ApiIgnore @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.success(preferenceService.get(requireUserId(userId))));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PreferenceDTO>> create(
            @ApiIgnore @AuthenticationPrincipal Long userId,
            @RequestBody PreferenceDTO request) {
        PreferenceDTO response = preferenceService.save(requireUserId(userId), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<PreferenceDTO>> update(
            @ApiIgnore @AuthenticationPrincipal Long userId,
            @RequestBody PreferenceDTO request) {
        return ResponseEntity.ok(
                ApiResponse.success(preferenceService.save(requireUserId(userId), request)));
    }

    private Long requireUserId(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.AUTH_REQUIRED);
        }
        return userId;
    }
}
