/**
 * 마이페이지 대시보드 조회 API.
 */
package org.tajiro.dashboard.controller;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tajiro.common.api.ApiResponse;
import org.tajiro.common.api.ErrorCode;
import org.tajiro.dashboard.dto.DashboardDTO;
import org.tajiro.dashboard.service.DashboardService;
import org.tajiro.exception.BusinessException;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping("/api/users/me/dashboard")
@RequiredArgsConstructor
@Api(tags = "12. 마이페이지", description = "사용자의 활동 현황을 확인하는 대시보드 조회 API")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<ApiResponse<DashboardDTO>> getDashboard(@ApiIgnore @AuthenticationPrincipal Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.AUTH_REQUIRED);
        }

        return ResponseEntity.ok(ApiResponse.success(dashboardService.getDashboard(userId)));
    }
}
