package org.tajiro.comparison.controller;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.tajiro.common.api.ErrorCode;
import org.tajiro.comparison.dto.ComparisonAnalysisRequestDTO;
import org.tajiro.comparison.dto.ComparisonAnalysisResponseDTO;
import org.tajiro.comparison.dto.ComparePropertyDTO;
import org.tajiro.comparison.dto.ComparisonMetricsResponseDTO;
import org.tajiro.comparison.service.ComparisonAiService;
import org.tajiro.comparison.service.ComparisonService;
import org.tajiro.exception.BusinessException;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Api(tags = "매물 비교")
public class ComparisonController {


    private final ComparisonService comparisonService;
    private final ComparisonAiService comparisonAiService;

    @GetMapping("/users/me/compare")
    public ResponseEntity<List<ComparePropertyDTO>> getCompareProperties(
            @ApiIgnore @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(comparisonService.getCompareProperties(requireUserId(userId)));
    }

    @PostMapping("/users/me/compare/{propertyId}")
    public ResponseEntity<Void> addCompareProperty(
            @ApiIgnore @AuthenticationPrincipal Long userId,
            @PathVariable Long propertyId) {
        comparisonService.addCompareProperty(requireUserId(userId), propertyId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/users/me/compare/{propertyId}")
    public ResponseEntity<Void> removeCompareProperty(
            @ApiIgnore @AuthenticationPrincipal Long userId,
            @PathVariable Long propertyId) {
        comparisonService.removeCompareProperty(requireUserId(userId), propertyId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/comparisons/metrics")
    public ResponseEntity<ComparisonMetricsResponseDTO> getComparisonMetrics(
            @ApiIgnore @AuthenticationPrincipal Long userId,
            @RequestParam List<Long> propertyIds,
            @RequestParam(required = false) Double workplaceLat,
            @RequestParam(required = false) Double workplaceLng) {
        return ResponseEntity.ok(
                comparisonService.getComparisonMetrics(
                        requireUserId(userId),
                        propertyIds,
                        workplaceLat,
                        workplaceLng));
    }

    @PostMapping("/comparisons/analyze")
    public ResponseEntity<ComparisonAnalysisResponseDTO> analyzeComparison(
            @ApiIgnore @AuthenticationPrincipal Long userId,
            @RequestBody ComparisonAnalysisRequestDTO request) {
        return ResponseEntity.ok(comparisonAiService.analyze(requireUserId(userId), request));
    }

    private Long requireUserId(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.AUTH_REQUIRED);
        }
        return userId;
    }
}
