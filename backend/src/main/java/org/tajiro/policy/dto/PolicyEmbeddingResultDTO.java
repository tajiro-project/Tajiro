package org.tajiro.policy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyEmbeddingResultDTO {

    private int categoryEmbeddingCount;
    private int policyEmbeddingCount;
    private int classifiedPolicyCount;
    private int remainingEmbeddingCount;
    private int remainingClassificationCount;
}