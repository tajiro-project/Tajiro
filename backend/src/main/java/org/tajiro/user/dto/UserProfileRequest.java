package org.tajiro.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserProfileRequest {
    private String targetRegion;
    private LocalDate birthDate;
    private Integer monthlyIncome;
    private Integer assetAmount;
    private String jobStatus;

    @JsonProperty("target_sgg_code")
    private String targetSggCode;
}
