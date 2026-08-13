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
public class LoginResponse {

    private String accessToken;
    private UserSummary user;

    public static LoginResponse of(String accessToken, UserVO vo) {
        return LoginResponse.builder()
                .accessToken(accessToken)
                .user(UserSummary.of(vo))
                .build();
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Data
    public static class UserSummary {
        private Long id;
        private String name;
        private String email;
        private String role;

        public static UserSummary of(UserVO vo) {
            if (vo == null) {
                return null;
            }
            return UserSummary.builder()
                    .id(vo.getId())
                    .name(vo.getName())
                    .email(vo.getEmail())
                    .role(vo.getRole())
                    .build();
        }
    }
}
