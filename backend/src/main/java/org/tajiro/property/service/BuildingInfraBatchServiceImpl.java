package org.tajiro.property.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Service;
import org.tajiro.property.domain.BuildingVO;
import org.tajiro.property.domain.InfrastructureDetailVO;
import org.tajiro.property.domain.InfrastructureVO;
import org.tajiro.property.infra.config.InfraCategoryConfig;
import org.tajiro.property.infra.config.InfraCategoryConfig.CategoryItem;
import org.tajiro.property.mapper.InfraMapper;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BuildingInfraBatchServiceImpl implements BuildingInfraBatchService {

    private static final int INFRA_RADIUS_M = 2000; // 중심 기준 반지름 2km (원형 범위)
    private static final int TOP_N_DETAIL = 3;

    // 병렬 처리를 위한 전용 스레드 풀 (카카오 API 호출용)
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    private final SqlSessionFactory sqlSessionFactory;
    private final KakaoSearchClient kakaoClient;

    @Override
    public void processPendingBuildings(int limit) {
        List<BuildingVO> targetBuildings;
        try (SqlSession session = sqlSessionFactory.openSession()) {
            InfraMapper mapper = session.getMapper(InfraMapper.class);
            targetBuildings = mapper.selectTargetBuildings(limit);
        }

        for (BuildingVO b : targetBuildings) {
            executeAggregationForBuilding(b);
        }
    }

    @Override
    public void processSingleBuilding(Long buildingId) {
        BuildingVO b;
        try (SqlSession session = sqlSessionFactory.openSession()) {
            InfraMapper mapper = session.getMapper(InfraMapper.class);
            b = mapper.selectBuildingById(buildingId);
        }
        if (b != null) {
            executeAggregationForBuilding(b);
        }
    }

    private void executeAggregationForBuilding(BuildingVO b) {
        if (b.getLatitude() == null || b.getLongitude() == null) {
            log.warn("건물 ID {} 의 위경도 좌표가 없습니다.", b.getId());
            return;
        }

        double lat = b.getLatitude().doubleValue();
        double lng = b.getLongitude().doubleValue();

        // 🚀 카테고리별 검색을 병렬(Parallel)로 동시 실행
        List<CompletableFuture<CategoryResult>> futures = InfraCategoryConfig.ALL.stream()
                .map(item -> CompletableFuture.supplyAsync(() -> {
                    try {
                        SearchResult result = "group".equals(item.getMethod())
                                ? searchGroup(item.getCode(), lat, lng)
                                : searchKeyword(item, lat, lng);
                        return new CategoryResult(item, result, null);
                    } catch (Exception e) {
                        return new CategoryResult(item, null, e);
                    }
                }, executorService))
                .collect(Collectors.toList());

        // 모든 카테고리 병렬 처리 완료 대기
        List<CategoryResult> results = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        // DB 작업 모아서 처리
        try (SqlSession session = sqlSessionFactory.openSession(false)) {
            InfraMapper mapper = session.getMapper(InfraMapper.class);

            for (CategoryResult res : results) {
                if (res.exception != null) {
                    log.error("건물 ID {} 의 카테고리 [{}] 수집 중 오류 발생", b.getId(), res.item.getKey(), res.exception);
                    continue;
                }
                saveInfrastructure(mapper, b.getId(), res.item.getKey(), res.result.count, res.result.docs, res.result.saturated);
            }

            session.commit();
        } catch (Exception e) {
            log.error("건물 ID {} 인프라 수집 처리 중 오류 발생", b.getId(), e);
        }
    }

    private SearchResult searchGroup(String code, double lat, double lng) throws Exception {
        JsonNode res = kakaoClient.searchCategory(code, lat, lng, INFRA_RADIUS_M);
        int totalCount = res.path("meta").path("total_count").asInt(0);
        int pageableCount = res.path("meta").path("pageable_count").asInt(0);

        List<JsonNode> docs = new ArrayList<>();
        if (res.has("documents")) {
            res.get("documents").forEach(docs::add);
        }

        boolean saturated = totalCount >= 45 || pageableCount >= 45;
        return new SearchResult(totalCount, docs, saturated);
    }

    private SearchResult searchKeyword(CategoryItem item, double lat, double lng) throws Exception {
        Map<String, JsonNode> found = new LinkedHashMap<>();
        int totalSumCount = 0;

        // 키워드가 여러 개인 경우 각 키워드의 1페이지(15개)만 병렬 또는 빠른 순회로 수집
        for (String q : item.getQueries()) {
            JsonNode res = kakaoClient.searchKeyword(q, lat, lng, INFRA_RADIUS_M, 1);
            totalSumCount += res.path("meta").path("total_count").asInt(0);

            if (res.has("documents")) {
                for (JsonNode doc : res.get("documents")) {
                    if (matched(doc, item)) {
                        found.put(doc.path("id").asText(), doc);
                    }
                }
            }
        }

        List<JsonNode> docs = new ArrayList<>(found.values());
        docs.sort(Comparator.comparingDouble(d -> d.path("distance").asDouble(0.0)));
        boolean saturated = totalSumCount >= 45;

        return new SearchResult(totalSumCount, docs, saturated);
    }

    private boolean matched(JsonNode doc, CategoryItem item) {
        String cat = doc.path("category_name").asText("");
        String name = doc.path("place_name").asText("");

        if (!item.getIncludes().isEmpty() && item.getIncludes().stream().noneMatch(cat::contains)) return false;
        if (!item.getExcludes().isEmpty() && item.getExcludes().stream().anyMatch(cat::contains)) return false;
        if (!item.getNameIncludes().isEmpty() && item.getNameIncludes().stream().noneMatch(name::contains)) return false;
        return true;
    }

    private void saveInfrastructure(InfraMapper mapper, Long buildingId, String key, int count, List<JsonNode> docs, boolean saturated) {
        Integer nearest = null;

        if (!docs.isEmpty()) {
            double distDouble = docs.get(0).path("distance").asDouble(0.0);
            if (distDouble > 0 || !docs.get(0).path("distance").asText("").isEmpty()) {
                nearest = (int) distDouble;
            }
        }

        InfrastructureVO infra = InfrastructureVO.builder()
                .buildingId(buildingId)
                .infraCategory(key)
                .countWithin2000m(count)
                .nearestDistanceMeters(nearest)
                .isSaturated(saturated)
                .build();

        mapper.upsertInfrastructure(infra);

        InfrastructureVO savedInfra = mapper.selectInfrastructureByBuildingAndCategory(buildingId, key);
        if (savedInfra == null) {
            log.warn("건물 ID {}, 카테고리 [{}] 요약 정보 저장 후 조회 실패", buildingId, key);
            return;
        }

        mapper.deleteInfrastructureDetailByInfraId(savedInfra.getId());

        int limit = Math.min(docs.size(), TOP_N_DETAIL);
        for (int i = 0; i < limit; i++) {
            JsonNode d = docs.get(i);

            InfrastructureDetailVO detail = InfrastructureDetailVO.builder()
                    .infraId(savedInfra.getId())
                    .infraName(d.path("place_name").asText(null))
                    .roadAddress(d.path("road_address_name").asText(null))
                    .latitude(BigDecimal.valueOf(d.path("y").asDouble(0.0)))
                    .longitude(BigDecimal.valueOf(d.path("x").asDouble(0.0)))
                    .distanceM((int) d.path("distance").asDouble(0.0))
                    .sortOrder(i + 1)
                    .build();

            mapper.insertInfrastructureDetail(detail);
        }
    }

    private static class SearchResult {
        int count;
        List<JsonNode> docs;
        boolean saturated;

        SearchResult(int count, List<JsonNode> docs, boolean saturated) {
            this.count = count;
            this.docs = docs;
            this.saturated = saturated;
        }
    }

    private static class CategoryResult {
        CategoryItem item;
        SearchResult result;
        Exception exception;

        CategoryResult(CategoryItem item, SearchResult result, Exception exception) {
            this.item = item;
            this.result = result;
            this.exception = exception;
        }
    }
}