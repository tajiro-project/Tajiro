package org.tajiro.market.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketEvaluationResult {
    private Long propertyId;
    private MarketEvaluationStatus status;
    private BigDecimal evaluationScore;
    private BigDecimal referencePrice;
    private Integer transactionCount;
    private Integer basisLevel;
    private boolean preserveExistingScore;
}
