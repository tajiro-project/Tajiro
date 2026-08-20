package org.tajiro.policy.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.tajiro.policy.batch.dto.YouthPolicyApiResponseDTO;
import org.tajiro.policy.batch.dto.YouthPolicyFetchResultDTO;
import org.tajiro.policy.batch.dto.YouthPolicyItemDTO;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
public class YouthPolicyApiClient {

    private final RestTemplate restTemplate;

    private final ObjectMapper objectMapper =
            new ObjectMapper();

    private final String apiUrl;
    private final String apiKey;
    private final int pageSize;


    public YouthPolicyApiClient(
            @Qualifier("openAiRestTemplate")
            RestTemplate restTemplate,

            @Value("${youth-policy.api-url}")
            String apiUrl,

            @Value("${youth-policy.api-key}")
            String apiKey,

            @Value("${youth-policy.page-size}")
            int pageSize
    ) {

        this.restTemplate = restTemplate;
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
        this.pageSize = pageSize;
    }


    public YouthPolicyFetchResultDTO fetchAll() {

        if (!hasText(apiKey)) {

            throw new IllegalStateException(
                    "청년정책 API 키가 설정되지 않았습니다."
            );
        }

        if (pageSize < 1 || pageSize > 3000) {

            throw new IllegalStateException(
                    "youth-policy.page-size는 1~3000 사이여야 합니다."
            );
        }


        List<YouthPolicyItemDTO> all =
                new ArrayList<>();

        Set<String> policyNos =
                new HashSet<>();

        Integer expectedTotalCount = null;

        int pageNum = 1;


        while (true) {

            YouthPolicyApiResponseDTO response =
                    fetchPage(pageNum);

            validateResponse(
                    response,
                    pageNum
            );


            int totalCount =
                    response
                            .getResult()
                            .getPagging()
                            .getTotCount();


            if (expectedTotalCount == null) {

                expectedTotalCount =
                        totalCount;

                /*
                 * API 이상 때문에 DB 전체가 지워지는 것 방지
                 */
                if (expectedTotalCount <= 0) {

                    throw new IllegalStateException(
                            "청년정책 API가 0건을 반환했습니다. "
                                    + "DB 정리를 중단합니다."
                    );
                }

            } else if (!expectedTotalCount.equals(totalCount)) {

                throw new IllegalStateException(
                        "청년정책 API 조회 중 총 건수가 변경되었습니다. "
                                + "expected="
                                + expectedTotalCount
                                + ", actual="
                                + totalCount
                );
            }


            List<YouthPolicyItemDTO> pagePolicies =
                    response
                            .getResult()
                            .getYouthPolicyList();


            if (pagePolicies == null
                    || pagePolicies.isEmpty()) {

                throw new IllegalStateException(
                        "전체 조회가 끝나기 전에 "
                                + "빈 페이지가 반환되었습니다. pageNum="
                                + pageNum
                );
            }


            for (YouthPolicyItemDTO item : pagePolicies) {

                if (!hasText(item.getPlcyNo())) {

                    throw new IllegalStateException(
                            "plcyNo가 없는 청년정책 데이터가 있습니다."
                    );
                }


                if (!policyNos.add(
                        item.getPlcyNo().trim()
                )) {

                    throw new IllegalStateException(
                            "중복 plcyNo가 있습니다: "
                                    + item.getPlcyNo()
                    );
                }


                all.add(item);
            }


            if (all.size() >= expectedTotalCount) {
                break;
            }

            pageNum++;
        }


        /*
         * 누락된 페이지가 없는지 최종 확인.
         * 이게 성공해야 이후 DELETE 가능.
         */
        if (all.size() != expectedTotalCount) {

            throw new IllegalStateException(
                    "청년정책 API 전체 조회 건수가 일치하지 않습니다. "
                            + "expected="
                            + expectedTotalCount
                            + ", actual="
                            + all.size()
            );
        }


        log.info(
                "청년정책 API 전체 조회 완료 count={}",
                all.size()
        );


        return new YouthPolicyFetchResultDTO(
                expectedTotalCount,
                all
        );
    }


    private YouthPolicyApiResponseDTO fetchPage(
            int pageNum
    ) {

        try {

            URI uri =
                    UriComponentsBuilder
                            .fromHttpUrl(apiUrl)
                            .queryParam(
                                    "apiKeyNm",
                                    apiKey
                            )
                            .queryParam(
                                    "pageNum",
                                    pageNum
                            )
                            .queryParam(
                                    "pageSize",
                                    pageSize
                            )
                            .queryParam(
                                    "rtnType",
                                    "json"
                            )
                            .build()
                            .encode()
                            .toUri();


            ResponseEntity<String> response =
                    restTemplate.getForEntity(
                            uri,
                            String.class
                    );


            if (!response
                    .getStatusCode()
                    .is2xxSuccessful()
                    || !hasText(response.getBody())) {

                throw new IllegalStateException(
                        "청년정책 API 응답이 올바르지 않습니다. status="
                                + response.getStatusCode()
                );
            }


            return objectMapper.readValue(
                    response.getBody(),
                    YouthPolicyApiResponseDTO.class
            );


        } catch (
                RestClientException
                | IOException e
        ) {

            log.warn(
                    "청년정책 API 호출/파싱 실패 pageNum={}",
                    pageNum,
                    e
            );

            throw new IllegalStateException(
                    "청년정책 API 조회에 실패했습니다.",
                    e
            );
        }
    }


    private void validateResponse(
            YouthPolicyApiResponseDTO response,
            int pageNum
    ) {

        if (response == null
                || response.getResultCode() == null
                || response.getResultCode() != 200
                || response.getResult() == null
                || response.getResult().getPagging() == null
                || response
                .getResult()
                .getPagging()
                .getTotCount() == null) {

            throw new IllegalStateException(
                    "청년정책 API 성공 응답이 아닙니다. pageNum="
                            + pageNum
            );
        }
    }


    private static boolean hasText(
            String value
    ) {

        return value != null
                && !value.trim().isEmpty();
    }
}