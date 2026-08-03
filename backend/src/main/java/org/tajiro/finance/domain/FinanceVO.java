/**
 * 금융상품 도메인 모델.
 */
package org.tajiro.finance.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;


@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class FinanceVO {

    private Long id;
    private String provider;
    private String productName;
    private BigDecimal minRate; //최소 금리
    private BigDecimal maxRate; //최고 금리
    private Integer maxLimitAmount; // 최대 한도 금액
    private String loanLimit; // 대출한도
    private String rateDescription;
    private String eligibility; //신청대상/제출 자격
    private String requiredDocs; // 제출 서류
    private String applicationUrl; // 신청 url
    private Boolean isActive; //판매여부
    private LocalDate referenceDate; //기준일
    private LocalDate validEndDate; // 종료일
    private LocalDate createdAt; // 등록일
}