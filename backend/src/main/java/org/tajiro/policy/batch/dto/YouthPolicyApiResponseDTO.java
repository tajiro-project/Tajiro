package org.tajiro.policy.batch.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
public class YouthPolicyApiResponseDTO {

    private Integer resultCode;
    private String resultMessage;
    private Result result;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    @NoArgsConstructor
    public static class Result {

        private Paging pagging;

        private List<YouthPolicyItemDTO> youthPolicyList =
                Collections.emptyList();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    @NoArgsConstructor
    public static class Paging {

        private Integer totCount;
        private Integer pageNum;
        private Integer pageSize;
    }
}