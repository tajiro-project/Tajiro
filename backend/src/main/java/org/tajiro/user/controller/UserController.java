/**
 * 내 정보 입력/조회 API (내정보입력).
 */
package org.tajiro.user.controller;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.tajiro.auth.service.AuthService;
import org.tajiro.common.api.ApiResponse;
import org.tajiro.common.api.ErrorCode;
import org.tajiro.exception.BusinessException;
import org.tajiro.user.dto.ProfileUpdateResponse;
import org.tajiro.user.dto.UserProfileDTO;
import org.tajiro.user.dto.UserProfileRequest;
import org.tajiro.user.service.UserProfileService;
import springfox.documentation.annotations.ApiIgnore;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
@Api(tags = "03. 내 정보", description = "사용자 프로필 조회, 수정 및 회원 탈퇴 API")
public class UserController {

    private final UserProfileService userProfileService;
    private final AuthService authService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileDTO>> getProfile(@ApiIgnore @AuthenticationPrincipal Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.AUTH_REQUIRED);
        }

        return ResponseEntity.ok(ApiResponse.success(userProfileService.getUserProfileById(userId)));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<ProfileUpdateResponse>> updateProfile(
            @ApiIgnore @AuthenticationPrincipal Long userId,
            @RequestBody UserProfileRequest request) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.AUTH_REQUIRED);
        }

        LocalDateTime updatedAt = userProfileService.saveProfile(userId, request);
        return ResponseEntity.ok(ApiResponse.success(ProfileUpdateResponse.builder().updatedAt(updatedAt).build()));
    }

    @DeleteMapping
    public ResponseEntity<Void> withdraw(@ApiIgnore @AuthenticationPrincipal Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.AUTH_REQUIRED);
        }

        authService.withdraw(userId);
        return ResponseEntity.noContent().build();
    }
}
