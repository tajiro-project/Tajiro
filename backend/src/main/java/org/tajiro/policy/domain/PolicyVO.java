/**
 * 정책, 정책 지역 도메인 모델.
 */
package org.tajiro.policy.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class PolicyVO{
    private Long id;
    private String title;
    private String region;
    private String minAge;
    private String maxAge;
    private Integer benefitAmount;
    private String applicationPeriod;
    private String agency;
    private String applyMethod;
    private String requiredDocuments;
    private String description;
    private String sumDescription;
    private String sbizCd;

    //임베딩
    private Long categoryId;
    private String categoryCode;
    private String categoryName;
    private BigDecimal categorySimilarity;
    private String embedding;
}

