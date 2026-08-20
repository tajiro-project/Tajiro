package org.tajiro.policy.batch.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
public class YouthPolicyItemDTO {

    private String plcyNo;
    private String plcyNm;
    private String plcyExplnCn;
    private String plcySprtCn;

    private String sprvsnInstCdNm;
    private String operInstCdNm;
    private String rgtrInstCdNm;
    private String rgtrUpInstCdNm;

    private String plcyAplyMthdCn;
    private String sbmsnDcmntCn;

    private String sprtTrgtMinAge;
    private String sprtTrgtMaxAge;

    private String zipCd;
    private String aplyYmd;
    private String lastMdfcnDt;
    private String sbizCd;
}