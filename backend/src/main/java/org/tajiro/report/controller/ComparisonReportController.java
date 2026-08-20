package org.tajiro.report.controller;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tajiro.common.api.ErrorCode;
import org.tajiro.exception.BusinessException;
import org.tajiro.report.dto.ComparisonReportCreateRequest;
import org.tajiro.report.dto.ComparisonReportResponse;
import org.tajiro.report.service.ComparisonReportService;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Api(tags = "09. 비교 리포트", description = "매물 비교 리포트 생성, 조회 및 삭제 API")
public class ComparisonReportController {


    private final ComparisonReportService comparisonReportService;

    @PostMapping("/users/me/comparison-reports")
    public ResponseEntity<ComparisonReportResponse> create(
            @ApiIgnore @AuthenticationPrincipal Long userId,
            @RequestBody ComparisonReportCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(comparisonReportService.create(requireUserId(userId), request));
    }

    @GetMapping("/users/me/comparison-reports")
    public ResponseEntity<List<ComparisonReportResponse>> getAll(
            @ApiIgnore @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(comparisonReportService.getAll(requireUserId(userId)));
    }

    @GetMapping("/users/me/comparison-reports/{reportId}")
    public ResponseEntity<ComparisonReportResponse> get(
            @ApiIgnore @AuthenticationPrincipal Long userId,
            @PathVariable Long reportId) {
        return ResponseEntity.ok(comparisonReportService.get(requireUserId(userId), reportId));
    }

    @DeleteMapping("/users/me/comparison-reports/{reportId}")
    public ResponseEntity<Void> delete(
            @ApiIgnore @AuthenticationPrincipal Long userId,
            @PathVariable Long reportId) {
        comparisonReportService.delete(requireUserId(userId), reportId);
        return ResponseEntity.noContent().build();
    }

    private Long requireUserId(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.AUTH_REQUIRED);
        }
        return userId;
    }
}
