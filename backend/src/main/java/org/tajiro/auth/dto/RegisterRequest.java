package org.tajiro.auth.dto;

import lombok.Data;

import java.util.List;

@Data
public class RegisterRequest {

    private String name;
    private String email;
    private String password;
    private Boolean isSeller;
    private String phone;
    private String agencyName;
    private List<AgreementRequest> agreements;

    @Data
    public static class AgreementRequest {
        private Long termsId;
        private Boolean agreed;
    }
}
