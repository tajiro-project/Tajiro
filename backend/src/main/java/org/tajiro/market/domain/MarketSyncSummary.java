package org.tajiro.market.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketSyncSummary {
    private int requestedMonths;
    private int successfulMonths;
    private int failedMonths;
    private int skippedCoveredMonths;
    private int storedTransactions;
}
