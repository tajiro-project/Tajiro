/**
 * 약관 목록/상세조회 API.
 */
package org.tajiro.terms.controller;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tajiro.common.api.ApiResponse;
import org.tajiro.terms.dto.TermsDTO;
import org.tajiro.terms.dto.TermsDetailDTO;
import org.tajiro.terms.service.TermsService;

import java.util.List;

@RestController
@RequestMapping("/api/terms")
@RequiredArgsConstructor
@Api(tags = "약관")
public class TermsController {

    private final TermsService termsService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TermsDTO>>> getTerms() {
        return ResponseEntity.ok(ApiResponse.success(termsService.getEffectiveTerms()));
    }

    @GetMapping("/{termsId}")
    public ResponseEntity<ApiResponse<TermsDetailDTO>> getTermsDetail(@PathVariable Long termsId) {
        return ResponseEntity.ok(ApiResponse.success(termsService.getTermsDetail(termsId)));
    }
}
