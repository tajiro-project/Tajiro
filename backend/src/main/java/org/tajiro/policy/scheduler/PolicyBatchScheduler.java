package org.tajiro.policy.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.tajiro.policy.batch.PolicyBatchService;
import org.tajiro.policy.batch.dto.PolicyBatchResultDTO;
import org.tajiro.policy.dto.PolicyEmbeddingResultDTO;
import org.tajiro.policy.service.PolicyEmbeddingService;

import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
@RequiredArgsConstructor
public class PolicyBatchScheduler {

    private final PolicyBatchService
            policyBatchService;

    private final PolicyEmbeddingService
            policyEmbeddingService;


    /*
     * 같은 서버에서 배치 중복 실행 방지
     */
    private final AtomicBoolean running =
            new AtomicBoolean(false);


    @Value("${policy.sync.enabled}")
    private boolean enabled;


    @Scheduled(
            cron = "${policy.sync.cron}",
            zone = "${policy.sync.zone}"
    )
    public void synchronize() {

        if (!enabled) {
            return;
        }


        if (!running.compareAndSet(
                false,
                true
        )) {

            log.warn(
                    "청년정책 배치가 이미 실행 중이라 "
                            + "이번 실행을 건너뜁니다."
            );

            return;
        }


        try {

            PolicyBatchResultDTO result =
                    policyBatchService
                            .synchronize();


            /*
             * 신규 또는 변경 정책이 있을 때만
             * 기존 embedding 로직 호출.
             */
            if (result.getInsertedCount() > 0
                    || result.getUpdatedCount() > 0) {

                PolicyEmbeddingResultDTO
                        embeddingResult =

                        policyEmbeddingService
                                .initializeAndClassify(
                                        3000
                                );


                log.info(
                        "청년정책 신규/변경 정책 "
                                + "임베딩 및 분류 완료 result={}",
                        embeddingResult
                );
            }


        } catch (Exception e) {

            log.error(
                    "청년정책 배치 실행 실패",
                    e
            );

        } finally {

            running.set(false);
        }
    }
}