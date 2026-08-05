package org.tajiro.comparison.controller;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.tajiro.comparison.dto.ComparePropertyDTO;
import org.tajiro.comparison.dto.ComparisonMetricsResponseDTO;
import org.tajiro.comparison.service.ComparisonService;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Api(tags = "매물 비교")
public class ComparisonController {

    // TODO: JWT 인증 구현 후 인증 객체에서 사용자 ID를 가져오도록 교체한다.
    private static final Long MOCK_USER_ID = 1L;

    private final ComparisonService comparisonService;

    @GetMapping("/users/me/compare")
    public ResponseEntity<List<ComparePropertyDTO>> getCompareProperties() {
        return ResponseEntity.ok(comparisonService.getCompareProperties(MOCK_USER_ID));
    }

    @PostMapping("/users/me/compare/{propertyId}")
    public ResponseEntity<Void> addCompareProperty(@PathVariable Long propertyId) {
        comparisonService.addCompareProperty(MOCK_USER_ID, propertyId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/users/me/compare/{propertyId}")
    public ResponseEntity<Void> removeCompareProperty(@PathVariable Long propertyId) {
        comparisonService.removeCompareProperty(MOCK_USER_ID, propertyId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/comparisons/metrics")
    public ResponseEntity<ComparisonMetricsResponseDTO> getComparisonMetrics(
            @RequestParam List<Long> propertyIds) {
        return ResponseEntity.ok(
                comparisonService.getComparisonMetrics(MOCK_USER_ID, propertyIds));
    }
}
