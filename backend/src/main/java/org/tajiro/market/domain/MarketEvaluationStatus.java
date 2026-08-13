package org.tajiro.market.domain;

public enum MarketEvaluationStatus {
    CALCULATED,
    DATA_INSUFFICIENT,
    NOT_SYNCED,
    SYNC_ERROR,
    SOURCE_UNMAPPED,
    REGION_CODE_MISSING
}
