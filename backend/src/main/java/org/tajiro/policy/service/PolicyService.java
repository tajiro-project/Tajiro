/**
 * 지역·나이 기준 정책 매칭 서비스.
 */
package org.tajiro.policy.service;

import org.tajiro.policy.dto.PolicyDTO;

import java.util.List;

public interface PolicyService{
    public List<PolicyDTO> getList(String keyword, String categoryCode, Long userId);

    public PolicyDTO get(Long policyId, Long userId);

    public List<PolicyDTO> getRecommendedByPropertyId(Long propertyId,Long userId);
}