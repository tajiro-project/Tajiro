/**
 * 프로필·가치관·대시보드 DTO.
 */
package org.tajiro.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.tajiro.user.domain.UserProfileVO;

import java.time.LocalDate;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class UserProfileDTO {
    private Long userId;
    private LocalDate birthDate;
    private String targetRegion;
    private Integer monthlyIncome;
    private Integer assetAmount;
    private String jobStatus;
    private LocalDateTime updatedAt;

    @JsonProperty("target_sgg_code")
    private String targetSggCode;

    public static UserProfileDTO of(UserProfileVO vo){
        if (vo==null){
            return null;
        }

        return UserProfileDTO.builder()
                .userId(vo.getUserId())
                .birthDate(vo.getBirthDate())
                .targetRegion(vo.getTargetRegion())
                .monthlyIncome(vo.getMonthlyIncome())
                .assetAmount(vo.getAssetAmount())
                .jobStatus(vo.getJobStatus())
                .updatedAt(vo.getUpdatedAt())
                .targetSggCode(vo.getTargetSggCode())
                .build();
    }
}