/**
 * 정책 목록·상세 DTO.
 */
package org.tajiro.policy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.tajiro.policy.domain.PolicyVO;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PolicyDTO {


    private Long id;
    private String title;
    private String region;
    private String minAge;
    private String maxAge;
    private Integer benefitAmount;
    private String applicationPeriod;
    private String agency;
    private String applyMethod;
    private String requiredDocuments;
    private String description;
    private String sumDescription;
    private String sbizCd;



    public static PolicyDTO of (PolicyVO vo){
        if (vo==null){
            return null;
        }
        return PolicyDTO.builder()
                .id(vo.getId())
                .title(vo.getTitle())
                .region(vo.getRegion())
                .minAge(vo.getMinAge())
                .maxAge(vo.getMaxAge())
                .benefitAmount(vo.getBenefitAmount())
                .applicationPeriod(vo.getApplicationPeriod())
                .agency(vo.getAgency())
                .applyMethod(vo.getApplyMethod())
                .requiredDocuments(vo.getRequiredDocuments())
                .description(vo.getDescription())
                .sumDescription(vo.getSumDescription())
                .sbizCd(vo.getSbizCd())
                .build();
    }
}
