package org.tajiro.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.tajiro.auth.domain.UserVO;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class UserInfoResponse {
    private String name;
    private String phone;
    private String agencyName;

    public static UserInfoResponse of(UserVO vo) {
        if (vo == null) {
            return null;
        }
        return UserInfoResponse.builder()
                .name(vo.getName())
                .phone(vo.getPhone())
                .agencyName(vo.getAgencyName())
                .build();
    }
}
