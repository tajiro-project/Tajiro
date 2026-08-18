/**
 * policy 도메인 MyBatis Mapper 인터페이스. XML은 resources/org/tajiro/policy/mapper/ 에 둡니다.
 */
package org.tajiro.policy.mapper;

import org.apache.ibatis.annotations.Param;
import org.tajiro.policy.domain.PolicyCategoryVO;
import org.tajiro.policy.domain.PolicyVO;

import java.math.BigDecimal;
import java.util.List;

public interface PolicyMapper{
    PolicyVO get(@Param("id") long id,
                 @Param("region") String region,
                 @Param("age") Integer age);

    List<PolicyVO> getList(@Param("region") String region,
            @Param("age") Integer age,
            @Param("keyword") String keyword,
            @Param("categoryCode") String categoryCode

    );

    // 카테고리 Embedding


    List<PolicyCategoryVO>
    getCategoriesWithoutEmbedding();


    List<PolicyCategoryVO>
    getCategoriesWithEmbedding();


    int updateCategoryEmbedding(
            @Param("id") Long id,
            @Param("embedding") String embedding
    );


    // Policy Embedding


    List<PolicyVO>
    getPoliciesWithoutEmbedding(

            @Param("limit") int limit
    );


    int updatePolicyEmbedding(

            @Param("id") Long id,
            @Param("embedding") String embedding
    );


    // Policy Classification


    List<PolicyVO>
    getPoliciesToClassify(

            @Param("limit") int limit
    );


    int updatePolicyClassification(
            @Param("id") Long id,
            @Param("categoryId") Long categoryId,
            @Param("categorySimilarity") BigDecimal categorySimilarity
    );


    int countPoliciesWithoutEmbedding();


    int countPoliciesWithoutClassification();
    List<PolicyVO> getHousingRecommendations(
            @Param("region") String region,
            @Param("age") Integer age
    );

    // 정책 배치
    List<PolicyVO> getLegacyPolicies();


    int deletePoliciesByIds(
            @Param("policyIds")
            List<Long> policyIds
    );


    List<PolicyVO> getBatchPolicySources();


    int insertBatchPolicy(
            PolicyVO policy
    );


    int updateBatchPolicy(
            PolicyVO policy
    );


    int deletePolicyTargetRegions(
            @Param("policyId")
            Long policyId
    );


    int insertPolicyTargetRegions(
            @Param("policyId")
            Long policyId,

            @Param("sggCodes")
            List<String> sggCodes
    );


    int deleteMissingBatchPolicies(
            @Param("activePolicyNos")
            List<String> activePolicyNos
    );
}
