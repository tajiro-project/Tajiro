/**
 * KB 금융상품 목록·상세·조건 매칭 API (12-2, 24).
 */
package org.tajiro.finance.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tajiro.finance.dto.FinanceDTO;
import org.tajiro.finance.dto.FinanceEmbeddingResultDTO;
import org.tajiro.finance.service.FinanceEmbeddingService;
import org.tajiro.finance.service.FinanceService;
import org.tajiro.finance.service.FinanceServiceImpl;

import java.util.List;

@RestController
@RequestMapping("/api/financial-products")
@RequiredArgsConstructor
@Api(tags = "KB금융 상품 관리")
public class FinanceController {
    final FinanceServiceImpl service;
    private final FinanceEmbeddingService embeddingService;

    @GetMapping("/matches")
    public ResponseEntity<List<FinanceDTO>> getList(@RequestParam(required = false) String keyword){

        return ResponseEntity.ok(service.getList(keyword));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FinanceDTO> get(@PathVariable("id") long id){

        return ResponseEntity.ok(service.get(id));
    }


    // 매물 거래유형 기반 금융상품 추천

    @GetMapping("/recommendations")
    @ApiOperation(
            "매물의 거래유형(월세/전세/매매)에 맞는 금융상품 추천"
    )
    public ResponseEntity<List<FinanceDTO>>
    getRecommendations(

            @RequestParam
            Long propertyId
    ) {

        return ResponseEntity.ok(
                service.getRecommendedByPropertyId(propertyId)
        );
    }


    /**
     * 9개 category embedding 생성
     * 63개 financial_product embedding 생성
     * cosine similarity 비교
     * 최종 category 저장
     */
    @PostMapping("/embeddings/classify")
    @ApiOperation(
            "금융상품/카테고리 임베딩 생성 및 9개 카테고리 분류"
    )
    public ResponseEntity<FinanceEmbeddingResultDTO>
    initializeEmbeddingsAndClassify() {

        return ResponseEntity.ok(
                embeddingService.initializeAndClassify()
        );
    }

}