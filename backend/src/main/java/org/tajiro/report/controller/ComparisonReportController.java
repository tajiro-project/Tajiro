package org.tajiro.report.controller;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tajiro.report.dto.ComparisonReportCreateRequest;
import org.tajiro.report.dto.ComparisonReportResponse;
import org.tajiro.report.service.ComparisonReportService;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Api(tags = "비교 리포트")
public class ComparisonReportController {

    // TODO: JWT 인증 구현 후 인증 객체에서 사용자 ID를 가져오도록 교체한다.
    private static final Long MOCK_USER_ID = 1L;

    private final ComparisonReportService comparisonReportService;

    @PostMapping("/users/me/comparison-reports")
    public ResponseEntity<ComparisonReportResponse> create(
            @RequestBody ComparisonReportCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(comparisonReportService.create(MOCK_USER_ID, request));
    }

    @GetMapping("/users/me/comparison-reports")
    public ResponseEntity<List<ComparisonReportResponse>> getAll() {
        return ResponseEntity.ok(comparisonReportService.getAll(MOCK_USER_ID));
    }

    @GetMapping("/users/me/comparison-reports/{reportId}")
    public ResponseEntity<ComparisonReportResponse> get(@PathVariable Long reportId) {
        return ResponseEntity.ok(comparisonReportService.get(MOCK_USER_ID, reportId));
    }

    @DeleteMapping("/users/me/comparison-reports/{reportId}")
    public ResponseEntity<Void> delete(@PathVariable Long reportId) {
        comparisonReportService.delete(MOCK_USER_ID, reportId);
        return ResponseEntity.noContent().build();
    }
}
