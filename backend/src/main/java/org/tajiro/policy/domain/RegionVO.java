package org.tajiro.policy.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RegionVO {
    private Long policyTargetRegionId;
    private Long policyId;
    private String sggCode;
    private LocalDate createAt;
}
