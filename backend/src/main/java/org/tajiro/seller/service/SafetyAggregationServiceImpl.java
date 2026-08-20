package org.tajiro.seller.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tajiro.property.domain.BuildingVO;
import org.tajiro.seller.domain.SafetyCategoryConfig;
import org.tajiro.seller.dto.SafetyAggregationDTO;
import org.tajiro.seller.dto.SafetyPoiDTO;
import org.tajiro.seller.mapper.SafetyAggregationMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class SafetyAggregationServiceImpl
        implements SafetyAggregationService {

    // 한 번의 INSERT가 너무 커지는 것을 막기 위한 크기
    private static final int DETAIL_BATCH_SIZE = 500;

    // 위도 1도 ≒ 111km
    private static final double METERS_PER_LATITUDE_DEGREE =
            111_000.0;

    private final SafetyAggregationMapper
            safetyAggregationMapper;


    /**
     * 아직 안전 집계가 완료되지 않은 건물 조회
     */
    @Override
    @Transactional(readOnly = true)
    public List<BuildingVO> findPendingBuildings(
            int limit
    ) {

        if (limit <= 0) {

            throw new IllegalArgumentException(
                    "limit은 1 이상이어야 합니다."
            );
        }

        return safetyAggregationMapper
                .findPendingBuildings(
                        SafetyCategoryConfig.CATEGORIES,
                        SafetyCategoryConfig.CATEGORIES.size(),
                        limit
                );
    }


    /**
     * 하나의 건물에 대해
     * 안전 데이터 전체 집계
     */
    @Override
    @Transactional
    public void aggregateBuilding(
            Long buildingId
    ) {

        /*
         * 1.
         * 건물 좌표 가져오기
         */
        BuildingVO building =
                safetyAggregationMapper
                        .findBuildingById(
                                buildingId
                        );

        if (building == null) {

            throw new IllegalArgumentException(
                    "존재하지 않는 buildingId입니다: "
                            + buildingId
            );
        }


        if (building.getLatitude() == null
                || building.getLongitude() == null) {

            throw new IllegalStateException(
                    "건물 좌표가 없습니다. buildingId="
                            + buildingId
            );
        }


        double lat =
                building.getLatitude()
                        .doubleValue();

        double lng =
                building.getLongitude()
                        .doubleValue();


        if (lat == 0.0
                || lng == 0.0) {

            throw new IllegalStateException(
                    "건물 좌표가 0입니다. buildingId="
                            + buildingId
            );
        }


        /*
         * 2.
         * 반경 500m 기준 bounding box 계산
         */
        BoundingBox box =
                calculateBoundingBox(
                        lat,
                        SafetyCategoryConfig.SAFETY_RADIUS_M
                );


        /*
         * 3.
         * 8개 안전 카테고리 집계
         *
         * CCTV:
         * count = 40
         * nearest = 41m
         *
         * 같은 식으로 반환됨
         */
        List<SafetyAggregationDTO> aggregations =
                safetyAggregationMapper
                        .aggregateSafety(
                                SafetyCategoryConfig.CATEGORIES,
                                lat,
                                lng,
                                box.dlat,
                                box.dlng,
                                SafetyCategoryConfig.SAFETY_RADIUS_M
                        );


        /*
         * 무조건 8개가 나와야 함.
         *
         * POI가 없는 카테고리도
         * count = 0으로 반환되어야 한다.
         */
        if (aggregations == null
                || aggregations.size()
                != SafetyCategoryConfig.CATEGORIES.size()) {

            int actualCount =
                    aggregations == null
                            ? 0
                            : aggregations.size();

            throw new IllegalStateException(
                    "안전 카테고리 집계 결과가 "
                            + SafetyCategoryConfig.CATEGORIES.size()
                            + "개가 아닙니다. buildingId="
                            + buildingId
                            + ", count="
                            + actualCount
            );
        }


        int totalDetailCount = 0;


        /*
         * 4.
         * 카테고리별 safety 저장
         */
        for (SafetyAggregationDTO aggregation
                : aggregations) {


            int countWithin500m =
                    aggregation.getCountWithin500m() == null
                            ? 0
                            : aggregation.getCountWithin500m();


            /*
             * safety
             *
             * 없으면 INSERT
             * 있으면 UPDATE
             */
            safetyAggregationMapper
                    .upsertSafety(
                            buildingId,
                            aggregation.getSafeCategory(),
                            countWithin500m,
                            aggregation.getNearestDistanceMeters()
                    );


            /*
             * 5.
             * 방금 저장된 safety의 PK 가져오기
             */
            Long safeId =
                    safetyAggregationMapper
                            .findSafetyId(
                                    buildingId,
                                    aggregation.getSafeCategory()
                            );


            if (safeId == null) {

                throw new IllegalStateException(
                        "safety id 조회에 실패했습니다. "
                                + "buildingId="
                                + buildingId
                                + ", category="
                                + aggregation.getSafeCategory()
                );
            }


            /*
             * 6.
             * 기존 상세 제거
             *
             * 재집계 시 데이터가 중복되면 안 되기 때문.
             *
             * count = 0이 된 경우에도
             * 예전 상세가 남지 않도록 항상 삭제한다.
             */
            safetyAggregationMapper
                    .deleteSafetyDetails(
                            safeId
                    );


            /*
             * 주변 POI가 없다면
             * safety 행만 남기고 다음 카테고리로 이동
             */
            if (countWithin500m == 0) {

                continue;
            }


            /*
             * 7.
             * 실제 500m 내부 POI 전부 조회
             */
            List<SafetyPoiDTO> details =
                    safetyAggregationMapper
                            .findSafetyPois(
                                    aggregation.getSafeCategory(),
                                    lat,
                                    lng,
                                    box.dlat,
                                    box.dlng,
                                    SafetyCategoryConfig.SAFETY_RADIUS_M
                            );


            /*
             * aggregateSafety에서 구한 개수와
             * 실제 상세 개수가 다르면 이상한 상태.
             */
            int detailCount =
                    details == null
                            ? 0
                            : details.size();


            if (detailCount != countWithin500m) {

                throw new IllegalStateException(
                        "안전 집계 개수와 상세 개수가 다릅니다. "
                                + "buildingId="
                                + buildingId
                                + ", category="
                                + aggregation.getSafeCategory()
                                + ", count="
                                + countWithin500m
                                + ", detailCount="
                                + detailCount
                );
            }


            /*
             * 가까운 순서대로
             *
             * sort_order = 1,2,3...
             */
            for (int i = 0;
                 i < details.size();
                 i++) {

                SafetyPoiDTO detail =
                        details.get(i);

                detail.setSafeId(
                        safeId
                );

                detail.setSortOrder(
                        i + 1
                );
            }


            /*
             * 8.
             * safety_detail 일괄 저장
             */
            insertDetailsInChunks(
                    details
            );


            totalDetailCount +=
                    details.size();
        }


        log.info(
                "안전 집계 완료 - "
                        + "buildingId={}, "
                        + "buildingCode={}, "
                        + "detailCount={}",
                building.getId(),
                building.getBuildingCode(),
                totalDetailCount
        );
    }


    /**
     * safety_detail batch INSERT
     *
     * CCTV처럼 수백 개가 존재할 수 있으므로
     * 500개 단위로 나눠 INSERT
     */
    private void insertDetailsInChunks(
            List<SafetyPoiDTO> details
    ) {

        if (details == null
                || details.isEmpty()) {

            return;
        }


        for (int from = 0;
             from < details.size();
             from += DETAIL_BATCH_SIZE) {


            int to =
                    Math.min(
                            from + DETAIL_BATCH_SIZE,
                            details.size()
                    );


            safetyAggregationMapper
                    .insertSafetyDetails(
                            details.subList(
                                    from,
                                    to
                            )
                    );
        }
    }


    /**
     * Python의 bbox() 대응
     *
     * 먼저 사각형 범위를 만들고
     * DB에서 그 안의 후보만 조회함.
     */
    private BoundingBox calculateBoundingBox(
            double latitude,
            int radiusMeters
    ) {

        double dlat =
                radiusMeters
                        / METERS_PER_LATITUDE_DEGREE;


        double latitudeRadian =
                Math.toRadians(
                        latitude
                );


        double cosLatitude =
                Math.cos(
                        latitudeRadian
                );


        if (Math.abs(cosLatitude)
                < 1e-12) {

            throw new IllegalArgumentException(
                    "극점에 가까운 위도는 지원하지 않습니다: "
                            + latitude
            );
        }


        double dlng =
                radiusMeters
                        / (
                        METERS_PER_LATITUDE_DEGREE
                                * cosLatitude
                );


        return new BoundingBox(
                dlat,
                dlng
        );
    }


    /**
     * bounding box용 내부 객체
     */
    private static class BoundingBox {

        private final double dlat;

        private final double dlng;


        private BoundingBox(
                double dlat,
                double dlng
        ) {

            this.dlat = dlat;
            this.dlng = dlng;
        }
    }
}