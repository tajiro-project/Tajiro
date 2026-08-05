/**
 * 금융상품 목록·상세 DTO.
 */
package org.tajiro.finance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.tajiro.finance.domain.FinanceVO;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class FinanceDTO {
    private Long id;
    private String provider;
    private String productName;
    private BigDecimal minRate; //최소 금리
    private BigDecimal maxRate; //최고 금리
    private Integer maxLimitAmount; // 최대 한도 금액
    private String loanLimit; // 대출한도
    private String rateDescription;
    private String eligibility; //신청대삭/제출 자격
    private String requiredDocs; // 제출 서류
    private String applicationUrl; // 신청 url
    private Boolean isActive; //판매여부
    private LocalDateTime referenceDate; //기준일
    private LocalDateTime validEndDate; // 종료일
    private LocalDateTime createdAt; // 등록일


    // VO  DTO 변환
    public static FinanceDTO of(FinanceVO vo) {
        if (vo == null) {
            return null;
        }

        return FinanceDTO.builder()
                .id(vo.getId())
                .provider(vo.getProvider())
                .productName(vo.getProductName())
                .minRate(vo.getMinRate())
                .maxRate(vo.getMaxRate())
                .maxLimitAmount(vo.getMaxLimitAmount())
                .loanLimit(vo.getLoanLimit())
                .rateDescription(vo.getRateDescription())
                .eligibility(vo.getEligibility())
                .requiredDocs(vo.getRequiredDocs())
                .applicationUrl(vo.getApplicationUrl())
                .isActive(vo.getIsActive())
                .referenceDate(vo.getReferenceDate())
                .validEndDate(vo.getValidEndDate())
                .createdAt(vo.getCreatedAt())
                .build();
    }
}