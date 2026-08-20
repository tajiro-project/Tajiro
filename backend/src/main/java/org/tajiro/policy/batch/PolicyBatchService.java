package org.tajiro.policy.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.tajiro.policy.batch.dto.PolicyBatchResultDTO;
import org.tajiro.policy.batch.dto.YouthPolicyFetchResultDTO;
import org.tajiro.policy.batch.dto.YouthPolicyItemDTO;
import org.tajiro.policy.client.PolicySummaryClient;
import org.tajiro.policy.client.YouthPolicyApiClient;
import org.tajiro.policy.domain.PolicyVO;
import org.tajiro.policy.mapper.PolicyMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PolicyBatchService {

    private static final DateTimeFormatter
            API_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern(
                    "yyyy-MM-dd HH:mm:ss"
            );


    private final YouthPolicyApiClient
            youthPolicyApiClient;

    private final PolicySummaryClient
            policySummaryClient;

    private final PolicyPeriodFilter
            policyPeriodFilter;

    private final PolicyBatchWriteService
            policyBatchWriteService;

    private final PolicyMapper
            policyMapper;


    @Value("${policy.summary.batch-size}")
    private int summaryBatchSize;


    @Value("${policy.sync.zone}")
    private String zone;


    /**
     * 청년정책 전체 동기화
     *
     * 흐름
     *
     * 1. 청년정책 API 전체 조회
     * 2. 신청기간 만료 정책 제거
     * 3. 저장 가능한 정책만 필터링
     * 4. 기존 DB 정책 조회
     * 5. 신규 / 변경 / 변경없음 판별
     * 6. 신규 / 변경 정책만 LLM 요약
     * 7. MyBatis INSERT / UPDATE
     * 8. policy_target_region 갱신
     * 9. 만료되거나 API에서 사라진 정책 삭제
     */
    public PolicyBatchResultDTO synchronize() {

        validateConfiguration();


        LocalDate today =
                LocalDate.now(
                        ZoneId.of(zone)
                );


        log.info(
                "청년정책 동기화 시작. today={}",
                today
        );


        /*
         * 기존 DB에 들어있던 정책 중
         * 신청기간이 끝난 정책 먼저 삭제
         */
        int legacyDeletedCount =
                deleteExpiredLegacyPolicies(
                        today
                );


        /*
         * =========================================
         * 1. 청년정책 API 전체 조회
         * =========================================
         */
        YouthPolicyFetchResultDTO fetchResult =
                youthPolicyApiClient.fetchAll();


        List<YouthPolicyItemDTO> fetchedPolicies =
                fetchResult.getPolicies();


        int fetchedCount =
                fetchedPolicies.size();


        log.info(
                "청년정책 API 조회 완료. fetchedCount={}",
                fetchedCount
        );


        /*
         * =========================================
         * 2. 신청기간 만료 정책 제거
         * =========================================
         */
        List<YouthPolicyItemDTO> periodActivePolicies =
                fetchedPolicies.stream()

                        .filter(
                                policy ->
                                        policyPeriodFilter
                                                .isActive(
                                                        policy.getAplyYmd(),
                                                        today
                                                )
                        )

                        .collect(
                                Collectors.toList()
                        );


        int expiredCount =
                fetchedCount
                        - periodActivePolicies.size();


        log.info(
                "신청기간 필터 완료. active={}, expired={}",
                periodActivePolicies.size(),
                expiredCount
        );


        /*
         * =========================================
         * 3. Tajiro DB에 저장 가능한 정책 필터링
         * =========================================
         *
         * 최소한
         *
         * - plcyNo
         * - 정책명
         * - 지원내용
         * - 정상적인 지역코드
         *
         * 가 있어야 저장한다.
         */
        List<YouthPolicyItemDTO> activePolicies =
                periodActivePolicies.stream()

                        .filter(
                                this::isUsablePolicy
                        )

                        .collect(
                                Collectors.toList()
                        );


        int skippedCount =
                periodActivePolicies.size()
                        - activePolicies.size();


        log.info(
                "정책 유효성 필터 완료. usable={}, skipped={}",
                activePolicies.size(),
                skippedCount
        );


        /*
         * 중복 plcyNo가 있는지 다시 방어
         */
        validateNoDuplicatePolicyNo(
                activePolicies
        );


        /*
         * =========================================
         * 4. DB 기존 정책 조회
         * =========================================
         *
         * getBatchPolicySources()에서는
         *
         * id
         * source_policy_no
         * source_modified_at
         *
         * 정도만 가져온다.
         *
         * 1600개 정책을 하나씩 SELECT 하지 않고
         * 한 번에 가져오기 위한 부분.
         */
        List<PolicyVO> existingPolicies =
                policyMapper
                        .getBatchPolicySources();


        Map<String, PolicyVO> existingByPolicyNo =
                new HashMap<>();


        for (PolicyVO policy
                : existingPolicies) {

            if (!hasText(
                    policy.getSourcePolicyNo()
            )) {
                continue;
            }


            existingByPolicyNo.put(
                    policy
                            .getSourcePolicyNo()
                            .trim(),

                    policy
            );
        }


        /*
         * =========================================
         * 5. 신규 / 변경 정책 찾기
         * =========================================
         */
        List<YouthPolicyItemDTO> changedPolicies =
                new ArrayList<>();


        int unchangedCount = 0;


        for (YouthPolicyItemDTO item
                : activePolicies) {

            String policyNo =
                    trim(
                            item.getPlcyNo()
                    );


            PolicyVO existing =
                    existingByPolicyNo.get(
                            policyNo
                    );


            /*
             * DB에 없으면 신규
             */
            if (existing == null) {

                changedPolicies.add(
                        item
                );

                continue;
            }


            /*
             * lastMdfcnDt가 바뀌었으면
             * 정책 내용이 수정된 것으로 판단
             */
            if (hasChanged(
                    existing,
                    item
            )) {

                changedPolicies.add(
                        item
                );

            } else {

                unchangedCount++;
            }
        }


        log.info(
                "정책 변경 감지 완료. changed={}, unchanged={}",
                changedPolicies.size(),
                unchangedCount
        );


        /*
         * =========================================
         * 6. 신규 / 변경 정책만 LLM 요약
         * =========================================
         */
        Map<String, String> summaries =
                generateSummariesInBatches(
                        changedPolicies
                );


        /*
         * =========================================
         * 7. 신규 / 변경 정책 저장
         * =========================================
         */
        int insertedCount = 0;
        int updatedCount = 0;


        for (YouthPolicyItemDTO item
                : changedPolicies) {

            String policyNo =
                    trim(
                            item.getPlcyNo()
                    );


            PolicyVO existing =
                    existingByPolicyNo.get(
                            policyNo
                    );


            String summary =
                    summaries.get(
                            policyNo
                    );


            if (!hasText(summary)) {

                /*
                 * LLM 요청 결과 누락 상황에서
                 * 이상한 데이터가 DB에 들어가는 걸 방지
                 */
                throw new IllegalStateException(
                        "정책 요약이 존재하지 않습니다. plcyNo="
                                + policyNo
                );
            }


            /*
             * 실제 INSERT / UPDATE와
             * policy_target_region 갱신은
             *
             * PolicyBatchWriteService에서
             * @Transactional 처리.
             */
            policyBatchWriteService
                    .savePolicy(
                            item,
                            summary,
                            existing
                    );


            if (existing == null) {

                insertedCount++;

            } else {

                updatedCount++;
            }
        }


        log.info(
                "정책 저장 완료. inserted={}, updated={}",
                insertedCount,
                updatedCount
        );


        /*
         * =========================================
         * 8. 현재 살아있는 정책번호 생성
         * =========================================
         *
         * 이 목록에 없는 기존 API 정책은 삭제한다.
         *
         * 즉:
         *
         * - 오늘 만료됨
         * - API에서 사라짐
         * - 저장 불가능한 상태로 변경됨
         *
         * → DB에서도 제거
         */
        List<String> activePolicyNos =
                activePolicies.stream()

                        .map(
                                YouthPolicyItemDTO::getPlcyNo
                        )

                        .filter(
                                PolicyBatchService::hasText
                        )

                        .map(
                                String::trim
                        )

                        .distinct()

                        .collect(
                                Collectors.toList()
                        );


        /*
         * API가 정상적으로 전체 조회된 이후에만
         * 여기까지 내려오기 때문에
         * cleanup 수행 가능.
         *
         * PolicyBatchWriteService에서도
         * 빈 리스트 DELETE 방어가 한 번 더 되어 있음.
         */
        int deletedCount =
                policyBatchWriteService
                        .deletePoliciesNotIn(
                                activePolicyNos
                        );


        log.info(
                "기존 만료/삭제 정책 정리 완료. deleted={}",
                deletedCount
        );


        /*
         * =========================================
         * 9. 결과 반환
         * =========================================
         */
        PolicyBatchResultDTO result =
                PolicyBatchResultDTO
                        .builder()

                        .fetchedCount(
                                fetchedCount
                        )

                        .activeCount(
                                activePolicies.size()
                        )

                        .expiredCount(
                                expiredCount
                        )

                        .skippedCount(
                                skippedCount
                        )

                        .insertedCount(
                                insertedCount
                        )

                        .updatedCount(
                                updatedCount
                        )

                        .unchangedCount(
                                unchangedCount
                        )

                        .deletedCount(
                                deletedCount
                        )

                        .build();


        log.info(
                "청년정책 동기화 완료. result={}",
                result
        );


        return result;
    }


    private int deleteExpiredLegacyPolicies(
            LocalDate today
    ) {

        List<PolicyVO> legacyPolicies =
                policyMapper.getLegacyPolicies();


        List<Long> expiredPolicyIds =
                legacyPolicies.stream()

                        .filter(
                                policy ->
                                        !policyPeriodFilter.isActive(
                                                policy.getApplicationPeriod(),
                                                today
                                        )
                        )

                        .map(
                                PolicyVO::getId
                        )

                        .collect(
                                Collectors.toList()
                        );


        if (expiredPolicyIds.isEmpty()) {

            log.info(
                    "기존 정책 만료 데이터 없음"
            );

            return 0;
        }


        int deletedCount =
                policyBatchWriteService
                        .deletePoliciesByIds(
                                expiredPolicyIds
                        );


        log.info(
                "기존 정책 만료 데이터 삭제 완료. deleted={}",
                deletedCount
        );


        return deletedCount;
    }


    /**
     * 변경된 정책을 일정 개수씩 나눠서
     * OpenAI로 보내 요약 생성.
     *
     * 예:
     *
     * changedPolicies = 47
     * batchSize = 20
     *
     * 1차 20개
     * 2차 20개
     * 3차 7개
     */
    private Map<String, String>
    generateSummariesInBatches(
            List<YouthPolicyItemDTO> policies
    ) {

        if (policies == null
                || policies.isEmpty()) {

            return Collections.emptyMap();
        }


        Map<String, String> summaries =
                new LinkedHashMap<>();


        for (
                int start = 0;
                start < policies.size();
                start += summaryBatchSize
        ) {

            int end =
                    Math.min(
                            start + summaryBatchSize,
                            policies.size()
                    );


            List<YouthPolicyItemDTO> batch =
                    policies.subList(
                            start,
                            end
                    );


            log.info(
                    "정책 LLM 요약 요청. start={}, end={}, count={}",
                    start,
                    end,
                    batch.size()
            );


            Map<String, String> batchResult =
                    policySummaryClient
                            .generateSummaries(
                                    batch
                            );


            /*
             * PolicySummaryClient에서 이미
             * 요청 개수 == 결과 개수를 검증하지만
             * 서비스에서도 한 번 더 확인.
             */
            if (batchResult.size()
                    != batch.size()) {

                throw new IllegalStateException(
                        "정책 요약 개수가 일치하지 않습니다. "
                                + "requested="
                                + batch.size()
                                + ", result="
                                + batchResult.size()
                );
            }


            summaries.putAll(
                    batchResult
            );
        }


        /*
         * 전체 개수 최종 검증
         */
        if (summaries.size()
                != policies.size()) {

            throw new IllegalStateException(
                    "전체 정책 요약 개수가 일치하지 않습니다. "
                            + "requested="
                            + policies.size()
                            + ", result="
                            + summaries.size()
            );
        }


        return summaries;
    }


    /**
     * API 정책과 DB 정책의 수정일 비교.
     *
     * source_modified_at
     *       vs
     * lastMdfcnDt
     */
    private boolean hasChanged(
            PolicyVO existing,
            YouthPolicyItemDTO incoming
    ) {

        if (existing == null) {
            return true;
        }


        LocalDateTime incomingModifiedAt =
                parseModifiedAt(
                        incoming.getLastMdfcnDt()
                );


        /*
         * API 수정일 형식이 이상하거나 비어있으면
         * "변경 없음"으로 잘못 판단하는 것보다
         * 다시 갱신하는 것이 안전하다.
         */
        if (incomingModifiedAt == null) {
            return true;
        }


        LocalDateTime existingModifiedAt =
                existing
                        .getSourceModifiedAt();


        if (existingModifiedAt == null) {
            return true;
        }


        return !incomingModifiedAt.equals(
                existingModifiedAt
        );
    }


    /**
     * 현재 Tajiro 정책 테이블에
     * 저장 가능한 정책인지 판단.
     */
    private boolean isUsablePolicy(
            YouthPolicyItemDTO policy
    ) {

        if (policy == null) {
            return false;
        }


        /*
         * API 정책 고유번호
         */
        if (!hasText(
                policy.getPlcyNo()
        )) {

            log.warn(
                    "정책 제외: plcyNo 없음"
            );

            return false;
        }


        /*
         * 정책명
         */
        if (!hasText(
                policy.getPlcyNm()
        )) {

            log.warn(
                    "정책 제외: 정책명 없음. plcyNo={}",
                    policy.getPlcyNo()
            );

            return false;
        }


        /*
         * description은
         *
         * plcySprtCn
         * 또는
         * plcyExplnCn
         *
         * 둘 중 하나는 있어야 한다.
         */
        if (!hasText(
                policy.getPlcySprtCn()
        )
                && !hasText(
                policy.getPlcyExplnCn()
        )) {

            log.warn(
                    "정책 제외: 지원내용 없음. plcyNo={}",
                    policy.getPlcyNo()
            );

            return false;
        }


        /*
         * Tajiro에서는 지역별 정책 추천을 하므로
         * 최소 하나의 유효한 시군구 코드가 필요.
         */
        if (!hasValidSggCode(
                policy.getZipCd()
        )) {

            log.warn(
                    "정책 제외: 유효한 zipCd 없음. "
                            + "plcyNo={}, zipCd={}",
                    policy.getPlcyNo(),
                    policy.getZipCd()
            );

            return false;
        }


        return true;
    }


    /**
     * zipCd 예:
     *
     * 12210,12240,12270
     */
    private boolean hasValidSggCode(
            String zipCd
    ) {

        if (!hasText(zipCd)) {
            return false;
        }


        String[] codes =
                zipCd.split(",");


        for (String code
                : codes) {

            if (hasText(code)
                    && code.trim()
                    .matches("\\d{5}")) {

                return true;
            }
        }


        return false;
    }


    /**
     * API lastMdfcnDt:
     *
     * 2026-07-31 17:53:25
     */
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

            log.warn(
                    "정책 수정일 파싱 실패. value={}",
                    value
            );

            return null;
        }
    }


    /**
     * API에서 같은 plcyNo가 여러 번 들어오는 경우
     * cleanup 결과가 이상해질 수 있으므로 중단.
     */
    private void validateNoDuplicatePolicyNo(
            List<YouthPolicyItemDTO> policies
    ) {

        Set<String> policyNos =
                new HashSet<>();


        for (YouthPolicyItemDTO policy
                : policies) {

            String policyNo =
                    trim(
                            policy.getPlcyNo()
                    );


            if (!policyNos.add(
                    policyNo
            )) {

                throw new IllegalStateException(
                        "중복된 정책번호가 있습니다. plcyNo="
                                + policyNo
                );
            }
        }
    }


    private void validateConfiguration() {

        if (summaryBatchSize <= 0) {

            throw new IllegalStateException(
                    "policy.summary.batch-size는 "
                            + "1 이상이어야 합니다."
            );
        }


        if (!hasText(zone)) {

            throw new IllegalStateException(
                    "policy.sync.zone이 설정되지 않았습니다."
            );
        }


        /*
         * 잘못된 zone 값이면 여기서 바로 예외 발생
         */
        ZoneId.of(
                zone
        );
    }


    private static String trim(
            String value
    ) {

        return value == null
                ? ""
                : value.trim();
    }


    private static boolean hasText(
            String value
    ) {

        return value != null
                && !value.trim().isEmpty();
    }
}
