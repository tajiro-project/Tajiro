package org.tajiro.preference.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tajiro.common.api.ErrorCode;
import org.tajiro.exception.BusinessException;
import org.tajiro.preference.domain.HousingPreferenceVO;
import org.tajiro.preference.domain.PreferencePriorityVO;
import org.tajiro.preference.dto.PreferenceDTO;
import org.tajiro.preference.mapper.PreferenceMapper;
import org.tajiro.property.domain.PropertyValueAnalysisResultVO;
import org.tajiro.property.mapper.PropertyScoreMapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PreferenceServiceImplTest {

    private InMemoryPreferenceMapper mapper;
    private InMemoryPropertyScoreMapper propertyScoreMapper;
    private PreferenceServiceImpl service;

    @BeforeEach
    void setUp() {
        mapper = new InMemoryPreferenceMapper();
        propertyScoreMapper = new InMemoryPropertyScoreMapper();
        service = new PreferenceServiceImpl(mapper, propertyScoreMapper);
    }

    @Test
    void savesAndReturnsWizardPreferences() {
        PreferenceDTO saved = service.save(1L, validPreference());

        assertEquals(Arrays.asList("원룸", "오피스텔"), saved.getHousingTypes());
        assertEquals(Arrays.asList("SUBWAY", "HOSPITAL"), saved.getDesiredInfraCategories());
        assertEquals("서울특별시 중구 세종대로 110", saved.getWorkplace().getAddress());
        assertEquals("COMMUTE", saved.getPriorities().get(0).getCriterion());
        assertEquals(2, mapper.priorities.size());
        assertEquals(1L, propertyScoreMapper.deletedUserId);
    }

    @Test
    void rejectsNonSequentialPriorityOrder() {
        PreferenceDTO request = validPreference();
        request.getPriorities().get(1).setPriorityOrder(1);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.save(1L, request));

        assertEquals(ErrorCode.INVALID_INPUT_VALUE, exception.getResponseCode());
    }

    @Test
    void returnsNotFoundWhenPreferenceDoesNotExist() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.get(1L));

        assertEquals(ErrorCode.PREFERENCE_NOT_FOUND, exception.getResponseCode());
    }

    private PreferenceDTO validPreference() {
        return PreferenceDTO.builder()
                .workplace(PreferenceDTO.Workplace.builder()
                        .name("서울시청")
                        .address("서울특별시 중구 세종대로 110")
                        .lat(new BigDecimal("37.5663"))
                        .lng(new BigDecimal("126.9779"))
                        .build())
                .hasCar(false)
                .maxCommuteDistanceMeters(1500)
                .housingTypes(Arrays.asList("원룸", "오피스텔"))
                .tradeTypes(Arrays.asList("월세", "전세"))
                .depositJeonseRange(Arrays.asList(0, 50000))
                .monthlyRentRange(Arrays.asList(0, 250))
                .salePriceRange(Arrays.asList(0, 400000))
                .areaRange(Arrays.asList(
                        new BigDecimal("0.0"), new BigDecimal("200.0")))
                .floorPreference(Collections.singletonList("2층 이상"))
                .desiredInfraCategories(Arrays.asList("SUBWAY", "HOSPITAL"))
                .desiredAmenityCategories(Arrays.asList("MART", "CAFE"))
                .priorities(Arrays.asList(
                        PreferenceDTO.Priority.builder()
                                .criterion("COMMUTE")
                                .priorityOrder(1)
                                .build(),
                        PreferenceDTO.Priority.builder()
                                .criterion("COST")
                                .priorityOrder(2)
                                .build()))
                .build();
    }

    private static class InMemoryPreferenceMapper implements PreferenceMapper {
        private HousingPreferenceVO preference;
        private List<PreferencePriorityVO> priorities = new ArrayList<>();

        @Override
        public HousingPreferenceVO findByUserId(Long userId) {
            return preference != null && userId.equals(preference.getUserId()) ? preference : null;
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
            int count = priorities.size();
            priorities.clear();
            return count;
        }

        @Override
        public int insertPriorities(List<PreferencePriorityVO> priorities) {
            this.priorities = new ArrayList<>(priorities);
            return priorities.size();
        }
    }

    private static class InMemoryPropertyScoreMapper implements PropertyScoreMapper {
        private Long deletedUserId;

        @Override
        public void upsertAll(List<PropertyValueAnalysisResultVO> scores) {
        }

        @Override
        public int deleteByUserId(Long userId) {
            deletedUserId = userId;
            return 1;
        }
    }
}
