package org.tajiro.market.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketSyncCoverageVO {
    private MarketApiSource sourceApi;
    private String sggCode;
    private String dealYm;
    private String lastAttemptStatus;
    private Integer totalCount;
    private LocalDateTime lastSuccessAt;
    private LocalDateTime lastAttemptAt;
    private String lastError;
}
