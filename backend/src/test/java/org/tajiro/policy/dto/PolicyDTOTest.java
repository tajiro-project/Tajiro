package org.tajiro.policy.dto;

import org.junit.jupiter.api.Test;
import org.tajiro.policy.domain.PolicyVO;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PolicyDTOTest {

    @Test
    void preservesMultipleSbizCodesAsString() {
        PolicyVO policy = PolicyVO.builder()
                .sbizCd("0014006,0014009")
                .build();

        PolicyDTO result = PolicyDTO.of(policy);

        assertEquals("0014006,0014009", result.getSbizCd());
    }
}
