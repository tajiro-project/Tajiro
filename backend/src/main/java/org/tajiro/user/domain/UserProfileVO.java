package org.tajiro.user.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class UserProfileVO {

    private Long userId;
    private LocalDate birthDate;
    private String targetRegion;
    private Integer mothlyIncome;
    private Integer assetAmount;
    private String jobStatus;
    private LocalDate updateAt;
    private String targetSggCode;

}