/**
 * 프로필·가치관·대시보드 DTO.
 */
package org.tajiro.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.tajiro.user.domain.UserProfileVO;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class UserProfileDTO {
    private Long userId;
    private LocalDate birthDate;
    private String targetRegion;
    private Integer mothlyIncome;
    private Integer assetAmount;
    private String jobStatus;
    private LocalDate updateAt;
    private String targetSggCode;

    public static UserProfileDTO of(UserProfileVO vo){
        if (vo==null){
            return null;
        }

        return UserProfileDTO.builder()
                .userId(vo.getUserId())
                .birthDate(vo.getBirthDate())
                .targetRegion(vo.getTargetRegion())
                .mothlyIncome(vo.getMothlyIncome())
                .assetAmount(vo.getAssetAmount())
                .jobStatus(vo.getJobStatus())
                .updateAt(vo.getUpdateAt())
                .targetSggCode(vo.getTargetSggCode())
                .build();
    }
}