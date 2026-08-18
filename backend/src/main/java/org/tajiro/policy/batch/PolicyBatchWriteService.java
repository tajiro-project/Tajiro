package org.tajiro.policy.batch;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tajiro.policy.batch.dto.YouthPolicyItemDTO;
import org.tajiro.policy.domain.PolicyVO;
import org.tajiro.policy.mapper.PolicyMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PolicyBatchWriteService {

    private static final DateTimeFormatter
            API_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern(
                    "yyyy-MM-dd HH:mm:ss"
            );


    private final PolicyMapper policyMapper;


    @Transactional
    public void savePolicy(
            YouthPolicyItemDTO item,
            String summary,
            PolicyVO existing
    ) {

        PolicyVO policy =
                toPolicyVO(
                        item,
                        summary,
                        existing
                );


        if (existing == null) {

            policyMapper.insertBatchPolicy(
                    policy
            );

        } else {

            policyMapper.updateBatchPolicy(
                    policy
            );
        }


        if (policy.getId() == null) {

            throw new IllegalStateException(
                    "정책 저장 후 id를 확인할 수 없습니다. plcyNo="
                            + item.getPlcyNo()
            );
        }


        /*
         * 변경된 지역정보 교체
         */
        policyMapper.deletePolicyTargetRegions(
                policy.getId()
        );


        List<String> sggCodes =
                parseSggCodes(
                        item.getZipCd()
                );


        if (!sggCodes.isEmpty()) {

            policyMapper
                    .insertPolicyTargetRegions(
                            policy.getId(),
                            sggCodes
                    );
        }
    }


    @Transactional
    public int deletePoliciesNotIn(
            List<String> activePolicyNos
    ) {

        if (activePolicyNos == null
                || activePolicyNos.isEmpty()) {

            /*
             * API 사고로 전체 policy 삭제되는 것 방지
             */
            throw new IllegalArgumentException(
                    "활성 정책 목록이 비어 있어 삭제를 중단합니다."
            );
        }


        return policyMapper
                .deleteMissingBatchPolicies(
                        activePolicyNos
                );
    }


    @Transactional
    public int deletePoliciesByIds(
            List<Long> policyIds
    ) {

        if (policyIds == null
                || policyIds.isEmpty()) {

            return 0;
        }


        return policyMapper.deletePoliciesByIds(
                policyIds
        );
    }


    private PolicyVO toPolicyVO(
            YouthPolicyItemDTO item,
            String summary,
            PolicyVO existing
    ) {

        return PolicyVO.builder()

                .id(
                        existing == null
                                ? null
                                : existing.getId()
                )

                .sourcePolicyNo(
                        trim(
                                item.getPlcyNo()
                        )
                )

                .sourceModifiedAt(
                        parseModifiedAt(
                                item.getLastMdfcnDt()
                        )
                )

                .title(
                        requiredText(
                                item.getPlcyNm(),
                                "정책명이 없습니다."
                        )
                )

                .region(
                        firstNonBlank(
                                item.getRgtrUpInstCdNm(),
                                item.getRgtrInstCdNm(),
                                item.getSprvsnInstCdNm(),
                                item.getOperInstCdNm()
                        )
                )

                .minAge(
                        normalizeAge(
                                item.getSprtTrgtMinAge()
                        )
                )

                .maxAge(
                        normalizeAge(
                                item.getSprtTrgtMaxAge()
                        )
                )

                .applicationPeriod(
                        trimToNull(
                                item.getAplyYmd()
                        )
                )

                .agency(
                        firstNonBlank(
                                item.getOperInstCdNm(),
                                item.getSprvsnInstCdNm(),
                                item.getRgtrInstCdNm()
                        )
                )

                .applyMethod(
                        trimToNull(
                                item.getPlcyAplyMthdCn()
                        )
                )

                .requiredDocuments(
                        trimToNull(
                                item.getSbmsnDcmntCn()
                        )
                )

                .description(
                        firstNonBlank(
                                item.getPlcySprtCn(),
                                item.getPlcyExplnCn(),
                                item.getPlcyNm()
                        )
                )

                .sumDescription(
                        requiredText(
                                summary,
                                "정책 요약이 없습니다."
                        )
                )

                .sbizCd(
                        trimToNull(
                                item.getSbizCd()
                        )
                )

                .build();
    }


    private LocalDateTime parseModifiedAt(
            String value
    ) {

        if (!hasText(value)) {
            return null;
        }

        try {

            return LocalDateTime.parse(
                    value.trim(),
                    API_DATE_TIME_FORMATTER
            );

        } catch (DateTimeParseException e) {

            return null;
        }
    }


    private List<String> parseSggCodes(
            String zipCd
    ) {

        if (!hasText(zipCd)) {

            return Collections.emptyList();
        }


        return Arrays
                .stream(
                        zipCd.split(",")
                )

                .map(String::trim)

                .filter(
                        code ->
                                code.matches("\\d{5}")
                )

                .distinct()

                .collect(
                        Collectors.toList()
                );
    }


    private String normalizeAge(
            String value
    ) {

        String trimmed =
                trim(value);

        return trimmed.matches("\\d+")
                ? trimmed
                : "0";
    }


    private String requiredText(
            String value,
            String message
    ) {

        if (!hasText(value)) {
            throw new IllegalArgumentException(message);
        }

        return value.trim();
    }


    private String firstNonBlank(
            String... values
    ) {

        for (String value : values) {

            if (hasText(value)) {

                return value.trim();
            }
        }

        return null;
    }


    private String trimToNull(
            String value
    ) {

        return hasText(value)
                ? value.trim()
                : null;
    }


    private String trim(
            String value
    ) {

        return value == null
                ? ""
                : value.trim();
    }


    private boolean hasText(
            String value
    ) {

        return value != null
                && !value.trim().isEmpty();
    }
}
