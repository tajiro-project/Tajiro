package org.tajiro.market.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketSyncTarget {
    private String sggCode;
    private String configuredCategory;
    private String propertyType;
    private String tradeType;
}
