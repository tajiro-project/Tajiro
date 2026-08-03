/**
 * policy 도메인 MyBatis Mapper 인터페이스. XML은 resources/org/tajiro/policy/mapper/ 에 둡니다.
 */
package org.tajiro.policy.mapper;

import org.apache.ibatis.annotations.Param;
import org.tajiro.policy.domain.PolicyVO;

import java.util.List;

public interface PolicyMapper{
    PolicyVO get(@Param("id") long id,
                 @Param("region") String region,
                 @Param("age") Integer age);

    List<PolicyVO> getList(@Param("region") String region,
            @Param("age") Integer age,
            @Param("keyword") String keyword
    );
}
