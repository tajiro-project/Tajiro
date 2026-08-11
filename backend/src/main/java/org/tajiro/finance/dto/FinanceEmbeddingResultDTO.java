package org.tajiro.finance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class FinanceEmbeddingResultDTO {

    // 새롭게 임베딩한 카테고리 개수
    private int categoryEmbeddingCount;

    // 새롭게 임베딩한 금융상품 개수
    private int productEmbeddingCount;

    // 카테고리 분류한 금융상품 개수
    private int classifiedProductCount;
}