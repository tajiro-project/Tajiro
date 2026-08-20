package org.tajiro.auth.dto;

import lombok.Data;

@Data
public class UserInfoUpdateRequest {
    private String name;
    private String phone;
    private String agencyName;
}
