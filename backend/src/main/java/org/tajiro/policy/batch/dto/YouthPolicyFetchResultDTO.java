package org.tajiro.policy.batch.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class YouthPolicyFetchResultDTO {

    private final int totalCount;
    private final List<YouthPolicyItemDTO> policies;
}