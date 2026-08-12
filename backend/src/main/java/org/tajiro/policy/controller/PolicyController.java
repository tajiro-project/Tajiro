/**
 * 청년 정책 목록·상세·자격 판정 API (12-1, 12-2, 23).
 */
package org.tajiro.policy.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.tajiro.finance.dto.FinanceDTO;
import org.tajiro.policy.dto.PolicyDTO;
import org.tajiro.policy.dto.PolicyEmbeddingResultDTO;
import org.tajiro.policy.service.PolicyEmbeddingService;
import org.tajiro.policy.service.PolicyService;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

@RestController
@RequestMapping("/api/policies")
@RequiredArgsConstructor
@Api(tags = "청년 정책 관리")
public class PolicyController{

    final PolicyService service;
    private final PolicyEmbeddingService policyEmbeddingService;

    @GetMapping("/matches")
    public ResponseEntity<List<PolicyDTO>> getMatches(
            @RequestParam(required = false) String keyword,

            @RequestParam(required = false) String categoryCode,

            @ApiIgnore @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.ok(service.getList(keyword,categoryCode,userId));
    }


    @GetMapping("/{policyId}")
    public ResponseEntity<PolicyDTO> get(
            @ApiIgnore @AuthenticationPrincipal Long userId,
//            @RequestHeader(value = "X-USER-ID", defaultValue = "1") Long userId,
            @PathVariable("policyId") long policyId)
    {

        return ResponseEntity.ok(service.get(policyId, userId));
    }

    @PostMapping("/embeddings/classify")
    @ApiOperation("정책/카테고리 임베딩 생성 및 9개 카테고리 분류")
    public ResponseEntity<PolicyEmbeddingResultDTO>
    classifyPolicies(

            @RequestParam(defaultValue = "100") int limit
    ) {

        return ResponseEntity.ok(
                policyEmbeddingService.initializeAndClassify(limit)
        );
    }
    @GetMapping("/recommendations")
    @ApiOperation(
            "매물 지역과 사용자 나이에 맞는 주거 정책 3개 추천"
    )
    public ResponseEntity<List<PolicyDTO>>
    getRecommendations(

            @RequestParam
            Long propertyId,

            @ApiIgnore @AuthenticationPrincipal
            Long userId
    ) {

        return ResponseEntity.ok(
                service.getRecommendedByPropertyId(
                        propertyId,
                        userId
                )
        );
    }
}