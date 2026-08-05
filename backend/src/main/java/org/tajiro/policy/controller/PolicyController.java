/**
 * 청년 정책 목록·상세·자격 판정 API (12-1, 12-2, 23).
 */
package org.tajiro.policy.controller;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.tajiro.finance.dto.FinanceDTO;
import org.tajiro.policy.dto.PolicyDTO;
import org.tajiro.policy.service.PolicyService;

import java.util.List;

@RestController
@RequestMapping("/api/policies")
@RequiredArgsConstructor
@Api(tags = "청년 정책 관리")
public class PolicyController{

    final PolicyService service;

    @GetMapping("/matches")
    public ResponseEntity<List<PolicyDTO>> getMatches(
//            @AuthenticationPrincipal Long userId,
            @RequestHeader(value = "X-USER-ID", defaultValue = "1") Long userId,
            @RequestParam(required = false) String keyword
    ) {
        return ResponseEntity.ok(service.getList(keyword,userId));
    }


    @GetMapping("")
    public ResponseEntity<PolicyDTO> get(
//            @AuthenticationPrincipal Long userId,
            @RequestHeader(value = "X-USER-ID", defaultValue = "1") Long userId,
            @RequestParam(required = true) Long policyId){

        return ResponseEntity.ok(service.get(policyId, userId));
    }



}