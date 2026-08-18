package org.tajiro.policy.batch.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class PolicyBatchResultDTO {

    private final int fetchedCount;
    private final int activeCount;
    private final int expiredCount;
    private final int skippedCount;

    private final int insertedCount;
    private final int updatedCount;
    private final int unchangedCount;
    private final int deletedCount;
}