package org.tajiro.property.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tajiro.common.api.ErrorCode;
import org.tajiro.exception.BusinessException;
import org.tajiro.preference.domain.HousingPreferenceVO;
import org.tajiro.preference.domain.PreferencePriorityVO;
import org.tajiro.preference.dto.PropertySearchRequest;
import org.tajiro.preference.mapper.PreferenceMapper;
import org.tajiro.property.domain.PropertyVO;
import org.tajiro.property.domain.PropertyValueAnalysisResultVO;
import org.tajiro.property.dto.PropertyComparisonScoreDTO;
import org.tajiro.property.mapper.PropertyMapper;
import org.tajiro.property.mapper.PropertyScoreMapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PropertyScoreServiceImplTest {

    private InMemoryPreferenceMapper preferenceMapper;
    private InMemoryPropertyMapper propertyMapper;
    private InMemoryPropertyScoreMapper propertyScoreMapper;
    private PropertyScoreService service;

    @BeforeEach
    void setUp() {
        preferenceMapper = new InMemoryPreferenceMapper();
        propertyMapper = new InMemoryPropertyMapper();
        propertyScoreMapper = new InMemoryPropertyScoreMapper();
        service = new PropertyScoreServiceImpl(
                preferenceMapper, propertyMapper, propertyScoreMapper);
    }

    @Test
    void calculatesAndSavesRankSumWeightedScore() {
        preferenceMapper.preference = preference();
        preferenceMapper.priorities = Arrays.asList(
                priority("COMMUTE", 1),
                priority("COST", 2),
                priority("INFRA", 3));

        PropertyVO property = PropertyVO.builder()
                .id(10L)
                .tradeType("월세")
                .deposit(500)
                .monthlyRent(20)
                .areaM2(new BigDecimal("30"))
                .distanceMeters(200)
                .desiredInfraCount(1)
                .desiredAmenityCount(0)
                .build();

        Map<Long, PropertyValueAnalysisResultVO> result = service.saveScores(
                1L, Collections.singletonList(property));

        // COMMUTE 80 * 3/6 + COST 68 * 2/6 + INFRA 50 * 1/6 = 71
        assertEquals(71, result.get(10L).getRecommendScore());
        assertEquals(200, result.get(10L).getWorkplaceDistanceMeters());
        assertEquals(1, propertyScoreMapper.savedScores.size());
        assertEquals(71, propertyScoreMapper.savedScores.get(0).getRecommendScore());
    }

    @Test
    void givesEveryCriterionEqualWeightWhenNoPriorityIsSelected() {
        preferenceMapper.preference = preference();
        preferenceMapper.priorities = Collections.emptyList();

        PropertyVO property = PropertyVO.builder()
                .id(10L)
                .tradeType("월세")
                .deposit(500)
                .monthlyRent(20)
                .areaM2(new BigDecimal("30"))
                .distanceMeters(200)
                .desiredInfraCount(1)
                .desiredAmenityCount(0)
                .build();

        int score = service.saveScores(1L, Collections.singletonList(property))
                .get(10L).getRecommendScore();

        // (COMMUTE 80 + COST 68 + INFRA 50 + AMENITY 0 + AREA 50) / 5 = 50
        assertEquals(50, score);
    }

    @Test
    void givesSameScoreToSamePropertyRegardlessOfBatchComposition() {
        preferenceMapper.preference = preference();
        preferenceMapper.priorities = Collections.singletonList(priority("AREA", 1));

        PropertyVO target = PropertyVO.builder()
                .id(10L)
                .areaM2(new BigDecimal("30"))
                .build();
        PropertyVO other = PropertyVO.builder()
                .id(20L)
                .areaM2(new BigDecimal("40"))
                .build();

        int singleScore = service.saveScores(1L, Collections.singletonList(target))
                .get(10L).getRecommendScore();
        int batchScore = service.saveScores(1L, Arrays.asList(target, other))
                .get(10L).getRecommendScore();

        assertEquals(50, singleScore);
        assertEquals(singleScore, batchScore);
    }

    @Test
    void usesEveryCategoryForCoverageScoreWhenNoCategoryIsSelected() {
        preferenceMapper.preference = preference();
        preferenceMapper.preference.setDesiredInfraCategories("");
        preferenceMapper.preference.setDesiredAmenityCategories(null);
        preferenceMapper.priorities = Arrays.asList(
                priority("INFRA", 1),
                priority("AMENITY", 2));

        PropertyVO property = PropertyVO.builder()
                .id(10L)
                .desiredInfraCount(8)
                .desiredAmenityCount(9)
                .build();

        int score = service.saveScores(1L, Collections.singletonList(property))
                .get(10L).getRecommendScore();

        // INFRA 8/16 = 50 * 2/3 + AMENITY 9/9 = 100 * 1/3 = 67
        assertEquals(67, score);
    }

    @Test
    void rejectsInvalidPriorityOrder() {
        preferenceMapper.preference = preference();
        preferenceMapper.priorities = Arrays.asList(
                priority("COMMUTE", 1),
                priority("COST", 1));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.saveScores(1L, Collections.singletonList(
                        PropertyVO.builder().id(10L).build())));

        assertEquals(ErrorCode.INVALID_INPUT_VALUE, exception.getResponseCode());
    }

    @Test
    void returnsEmptyResultWithoutWritingWhenNoPropertiesExist() {
        Map<Long, PropertyValueAnalysisResultVO> result = service.saveScores(
                1L, Collections.emptyList());

        assertEquals(Collections.emptyMap(), result);
        assertFalse(propertyScoreMapper.upsertCalled);
    }

    @Test
    void recalculatesEveryPropertyWithoutApplyingPreferenceFilters() {
        preferenceMapper.preference = preference();
        preferenceMapper.priorities = Collections.singletonList(priority("AREA", 1));
        propertyMapper.properties = Collections.singletonList(PropertyVO.builder()
                .id(10L)
                .areaM2(new BigDecimal("30"))
                .distanceMeters(200)
                .build());

        service.recalculateAllScores(1L);

        assertEquals(1L, propertyMapper.request.getUserId());
        assertFalse(propertyMapper.request.isApplyDesiredCategoryFilter());
        assertEquals(true, propertyMapper.request.isUseAllCategoriesWhenEmpty());
        assertEquals(1, propertyScoreMapper.savedScores.size());
        assertEquals(10L, propertyScoreMapper.savedScores.get(0).getPropertyId());
    }

    @Test
    void calculatesSelectedComparisonScoresWithoutPersistingThem() {
        preferenceMapper.preference = preference();
        preferenceMapper.priorities = Arrays.asList(
                priority("COMMUTE", 1),
                priority("COST", 2));
        propertyMapper.properties = Collections.singletonList(PropertyVO.builder()
                .id(10L)
                .tradeType("월세")
                .deposit(500)
                .monthlyRent(20)
                .distanceMeters(200)
                .build());

        PropertyComparisonScoreDTO score = service.calculateComparisonScores(
                1L,
                Collections.singletonList(10L),
                new BigDecimal("37.5000"),
                new BigDecimal("127.0000"))
                .get(10L);

        assertEquals(Collections.singletonList(10L), propertyMapper.request.getPropertyIds());
        assertEquals(new BigDecimal("37.5000"), propertyMapper.request.getRefLat());
        assertEquals(76, score.getPreferenceScore());
        assertEquals(80, score.getCommuteScore());
        assertEquals(68, score.getCostScore());
        assertEquals(0, score.getInfraScore());
        assertEquals(0, score.getAmenityScore());
        assertFalse(propertyScoreMapper.upsertCalled);
    }

    private HousingPreferenceVO preference() {
        return HousingPreferenceVO.builder()
                .userId(1L)
                .minDeposit(0)
                .maxDeposit(1000)
                .minMonthlyRent(0)
                .maxMonthlyRent(100)
                .minSellingPrice(0)
                .maxSellingPrice(10000)
                .minArea(new BigDecimal("20"))
                .maxArea(new BigDecimal("40"))
                .maxWorkplaceDistanceMeters(1000)
                .workplaceLatitude(new BigDecimal("37.5663"))
                .workplaceLongitude(new BigDecimal("126.9779"))
                .desiredInfraCategories("SUBWAY,HOSPITAL")
                .desiredAmenityCategories("MART,CAFE")
                .build();
    }

    private static class InMemoryPropertyMapper implements PropertyMapper {
        private PropertySearchRequest request;
        private List<PropertyVO> properties = Collections.emptyList();

        @Override
        public List<PropertyVO> getList(PropertySearchRequest request) {
            this.request = request;
            return properties;
        }
    }

    private PreferencePriorityVO priority(String criterion, int order) {
        return PreferencePriorityVO.builder()
                .userId(1L)
                .criterion(criterion)
                .priorityOrder(order)
                .build();
    }

    private static class InMemoryPreferenceMapper implements PreferenceMapper {
        private HousingPreferenceVO preference;
        private List<PreferencePriorityVO> priorities = new ArrayList<>();

        @Override
        public HousingPreferenceVO findByUserId(Long userId) {
            return preference;
        }

        @Override
        public List<PreferencePriorityVO> findPrioritiesByUserId(Long userId) {
            return new ArrayList<>(priorities);
        }

        @Override
        public int upsert(HousingPreferenceVO preference) {
            this.preference = preference;
            return 1;
        }

        @Override
        public int deletePrioritiesByUserId(Long userId) {
            priorities.clear();
            return 1;
        }

        @Override
        public int insertPriorities(List<PreferencePriorityVO> priorities) {
            this.priorities = new ArrayList<>(priorities);
            return priorities.size();
        }
    }

    private static class InMemoryPropertyScoreMapper implements PropertyScoreMapper {
        private boolean upsertCalled;
        private List<PropertyValueAnalysisResultVO> savedScores = new ArrayList<>();

        @Override
        public void upsertAll(List<PropertyValueAnalysisResultVO> scores) {
            upsertCalled = true;
            savedScores = new ArrayList<>(scores);
        }

    }
}
