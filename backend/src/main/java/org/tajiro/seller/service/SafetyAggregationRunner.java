package org.tajiro.seller.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.tajiro.property.domain.BuildingVO;

import java.util.List;

@Component
@RequiredArgsConstructor
@Log4j2
public class SafetyAggregationRunner {

    private final SafetyAggregationService
            safetyAggregationService;


    /*
     * 한 번에 처리할 건물 개수
     *
     * application.properties에 값이 없으면
     * 기본 20개
     */
    @Value("${safety.aggregation.batch-size:20}")
    private int batchSize;


    /*
     * 기본값
     *
     * 서버 시작 10초 후 첫 실행
     * 이후 이전 작업 종료 후 60초 뒤 다시 실행
     */
    @Scheduled(
            fixedDelayString =
                    "${safety.aggregation.fixed-delay-ms:60000}",

            initialDelayString =
                    "${safety.aggregation.initial-delay-ms:10000}"
    )
    public void run() {

        /*
         * 안전 카테고리 8개가
         * 아직 모두 저장되지 않은 건물 조회
         */
        List<BuildingVO> buildings =
                safetyAggregationService
                        .findPendingBuildings(
                                batchSize
                        );


        if (buildings.isEmpty()) {

            return;
        }


        log.info(
                "안전 집계 배치 시작 - targetCount={}",
                buildings.size()
        );


        int successCount = 0;

        int failCount = 0;


        /*
         * 건물별 처리
         */
        for (BuildingVO building
                : buildings) {

            try {

                safetyAggregationService
                        .aggregateBuilding(
                                building.getId()
                        );


                successCount++;

            } catch (Exception e) {

                failCount++;


                /*
                 * 하나 실패했다고
                 * 전체 배치를 중단하지 않는다.
                 */
                log.error(
                        "안전 집계 실패 - "
                                + "buildingId={}, "
                                + "buildingCode={}",
                        building.getId(),
                        building.getBuildingCode(),
                        e
                );
            }
        }


        log.info(
                "안전 집계 배치 종료 - "
                        + "successCount={}, "
                        + "failCount={}",
                successCount,
                failCount
        );
    }
}