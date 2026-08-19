package org.tajiro.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class RegisterRequest {

    private String name;
    private String email;
    private String password;
    private Boolean isSeller;
    private String phone;
    private String agencyName;
    private String targetRegion;
    private LocalDate birthDate;

    @JsonProperty("target_sgg_code")
    private String targetSggCode;

    private List<AgreementRequest> agreements;

    @Data
    public static class AgreementRequest {
        private Long termsId;
        private Boolean agreed;
    }
}
