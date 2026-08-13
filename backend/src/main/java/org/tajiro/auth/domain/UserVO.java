package org.tajiro.auth.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class UserVO {
    private Long id;
    private String email;
    private String passwordHash;
    private String name;
    private String role;
    private String phone;
    private String agencyName;
    private String provider;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;
}
