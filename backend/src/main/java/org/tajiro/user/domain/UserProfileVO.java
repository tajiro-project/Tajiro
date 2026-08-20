package org.tajiro.user.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class UserProfileVO {

    private Long userId;
    private LocalDate birthDate;
    private String targetRegion;
    private Integer monthlyIncome;
    private Integer assetAmount;
    private String jobStatus;
    private LocalDateTime updatedAt;
    private String targetSggCode;

}