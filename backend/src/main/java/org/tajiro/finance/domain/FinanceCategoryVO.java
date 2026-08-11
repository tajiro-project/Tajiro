package org.tajiro.finance.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class FinanceCategoryVO {

    private Long id;
    private String tradeType; // 월세 / 전세 / 매매
    private String productType; // 담보 / 보증 / 기타
    private String description; // 카테고리 설명
    private String embedding; // description의 embedding
}