package org.tajiro.preference.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tajiro.common.api.ErrorCode;
import org.tajiro.exception.BusinessException;
import org.tajiro.preference.domain.HousingPreferenceVO;
import org.tajiro.preference.domain.PreferencePriorityVO;
import org.tajiro.preference.dto.PreferenceDTO;
import org.tajiro.preference.mapper.PreferenceMapper;
import org.tajiro.property.mapper.PropertyScoreMapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PreferenceServiceImpl implements PreferenceService {

    private static final Set<String> HOUSING_TYPES = setOf("원룸", "아파트", "주택/빌라", "오피스텔");
    private static final Set<String> TRADE_TYPES = setOf("월세", "전세", "매매");
    private static final Set<String> FLOOR_TYPES = setOf("지하/반지하", "1층", "2층 이상", "옥탑");
    private static final Set<String> INFRA_CATEGORIES = setOf(
            "SUBWAY", "BUS_TERMINAL", "TRAIN", "HOSPITAL", "PHARMACY", "SCHOOL",
            "KINDERGARTEN", "ACADEMY", "LIBRARY", "PARK", "POLICE", "FIRE",
            "GOV_OFFICE", "PUBLIC", "POST_OFFICE", "BANK");
    private static final Set<String> AMENITY_CATEGORIES = setOf(
            "CONVENIENCE", "MART", "CAFE", "FOOD", "CULTURE", "SPORTS",
            "SWIMMING", "PARKING", "GAS");
    private static final Set<String> PRIORITY_CRITERIA = setOf(
            "COMMUTE", "COST", "INFRA", "AMENITY", "AREA");

    private final PreferenceMapper preferenceMapper;
    private final PropertyScoreMapper propertyScoreMapper;

    @Override
    @Transactional(readOnly = true)
    public PreferenceDTO get(Long userId) {
        HousingPreferenceVO preference = preferenceMapper.findByUserId(userId);
        if (preference == null) {
            throw new BusinessException(ErrorCode.PREFERENCE_NOT_FOUND);
        }

        return toDto(preference, preferenceMapper.findPrioritiesByUserId(userId));
    }

    @Override
    @Transactional
    public PreferenceDTO save(Long userId, PreferenceDTO request) {
        validate(request);

        preferenceMapper.upsert(toVO(userId, request));
        preferenceMapper.deletePrioritiesByUserId(userId);

        List<PreferencePriorityVO> priorities = request.getPriorities().stream()
                .map(priority -> PreferencePriorityVO.builder()
                        .userId(userId)
                        .criterion(priority.getCriterion())
                        .priorityOrder(priority.getPriorityOrder())
                        .build())
                .collect(Collectors.toList());
        preferenceMapper.insertPriorities(priorities);
        propertyScoreMapper.deleteByUserId(userId);

        return get(userId);
    }

    private HousingPreferenceVO toVO(Long userId, PreferenceDTO request) {
        PreferenceDTO.Workplace workplace = request.getWorkplace();
        return HousingPreferenceVO.builder()
                .userId(userId)
                .housingTypes(join(request.getHousingTypes()))
                .tradeTypes(join(request.getTradeTypes()))
                .minDeposit(request.getDepositJeonseRange().get(0))
                .maxDeposit(request.getDepositJeonseRange().get(1))
                .minMonthlyRent(request.getMonthlyRentRange().get(0))
                .maxMonthlyRent(request.getMonthlyRentRange().get(1))
                .minSellingPrice(request.getSalePriceRange().get(0))
                .maxSellingPrice(request.getSalePriceRange().get(1))
                .minArea(request.getAreaRange().get(0))
                .maxArea(request.getAreaRange().get(1))
                .floorPreference(join(request.getFloorPreference()))
                .maxWorkplaceDistanceMeters(request.getMaxCommuteDistanceMeters())
                .desiredInfraCategories(join(request.getDesiredInfraCategories()))
                .desiredAmenityCategories(join(request.getDesiredAmenityCategories()))
                .hasCar(request.getHasCar())
                .workplaceLatitude(workplace.getLat())
                .workplaceLongitude(workplace.getLng())
                .workplaceAddress(workplace.getAddress().trim())
                .build();
    }

    private PreferenceDTO toDto(HousingPreferenceVO preference, List<PreferencePriorityVO> priorities) {
        List<PreferencePriorityVO> safePriorities = priorities == null
                ? Collections.emptyList()
                : priorities;

        return PreferenceDTO.builder()
                .workplace(PreferenceDTO.Workplace.builder()
                        .name(preference.getWorkplaceAddress())
                        .address(preference.getWorkplaceAddress())
                        .lat(preference.getWorkplaceLatitude())
                        .lng(preference.getWorkplaceLongitude())
                        .build())
                .hasCar(preference.getHasCar())
                .maxCommuteDistanceMeters(preference.getMaxWorkplaceDistanceMeters())
                .housingTypes(split(preference.getHousingTypes()))
                .tradeTypes(split(preference.getTradeTypes()))
                .depositJeonseRange(Arrays.asList(preference.getMinDeposit(), preference.getMaxDeposit()))
                .monthlyRentRange(Arrays.asList(
                        preference.getMinMonthlyRent(), preference.getMaxMonthlyRent()))
                .salePriceRange(Arrays.asList(
                        preference.getMinSellingPrice(), preference.getMaxSellingPrice()))
                .areaRange(Arrays.asList(preference.getMinArea(), preference.getMaxArea()))
                .floorPreference(split(preference.getFloorPreference()))
                .desiredInfraCategories(split(preference.getDesiredInfraCategories()))
                .desiredAmenityCategories(split(preference.getDesiredAmenityCategories()))
                .priorities(safePriorities.stream()
                        .map(priority -> PreferenceDTO.Priority.builder()
                                .criterion(priority.getCriterion())
                                .priorityOrder(priority.getPriorityOrder())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    private void validate(PreferenceDTO request) {
        if (request == null
                || request.getHasCar() == null
                || request.getWorkplace() == null
                || isBlank(request.getWorkplace().getAddress())
                || !between(request.getWorkplace().getLat(), BigDecimal.valueOf(-90), BigDecimal.valueOf(90))
                || !between(request.getWorkplace().getLng(), BigDecimal.valueOf(-180), BigDecimal.valueOf(180))
                || !between(request.getMaxCommuteDistanceMeters(), 0, 10000)
                || !validValues(request.getHousingTypes(), HOUSING_TYPES)
                || !validValues(request.getTradeTypes(), TRADE_TYPES)
                || !validRange(request.getDepositJeonseRange(), 0, 50000)
                || !validRange(request.getMonthlyRentRange(), 0, 250)
                || !validRange(request.getSalePriceRange(), 0, 400000)
                || !validDecimalRange(
                        request.getAreaRange(), BigDecimal.ZERO, BigDecimal.valueOf(200))
                || !validValues(request.getFloorPreference(), FLOOR_TYPES)
                || !validValues(request.getDesiredInfraCategories(), INFRA_CATEGORIES)
                || !validValues(request.getDesiredAmenityCategories(), AMENITY_CATEGORIES)
                || !validPriorities(request.getPriorities())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private boolean validPriorities(List<PreferenceDTO.Priority> priorities) {
        if (priorities == null || priorities.isEmpty() || priorities.size() > 3) {
            return false;
        }

        Set<String> criteria = new HashSet<>();
        Set<Integer> orders = new HashSet<>();
        for (PreferenceDTO.Priority priority : priorities) {
            if (priority == null
                    || !PRIORITY_CRITERIA.contains(priority.getCriterion())
                    || priority.getPriorityOrder() == null
                    || priority.getPriorityOrder() < 1
                    || priority.getPriorityOrder() > priorities.size()
                    || !criteria.add(priority.getCriterion())
                    || !orders.add(priority.getPriorityOrder())) {
                return false;
            }
        }
        return true;
    }

    private boolean validValues(List<String> values, Set<String> allowed) {
        return values != null
                && values.stream().noneMatch(value -> value == null || !allowed.contains(value))
                && new HashSet<>(values).size() == values.size();
    }

    private boolean validRange(List<Integer> range, int min, int max) {
        return range != null
                && range.size() == 2
                && range.get(0) != null
                && range.get(1) != null
                && range.get(0) >= min
                && range.get(1) <= max
                && range.get(0) <= range.get(1);
    }

    private boolean validDecimalRange(List<BigDecimal> range, BigDecimal min, BigDecimal max) {
        return range != null
                && range.size() == 2
                && range.get(0) != null
                && range.get(1) != null
                && range.get(0).compareTo(min) >= 0
                && range.get(1).compareTo(max) <= 0
                && range.get(0).compareTo(range.get(1)) <= 0;
    }

    private boolean between(BigDecimal value, BigDecimal min, BigDecimal max) {
        return value != null
                && value.compareTo(min) >= 0
                && value.compareTo(max) <= 0;
    }

    private boolean between(Number value, double min, double max) {
        if (value == null) {
            return false;
        }
        double number = value.doubleValue();
        return Double.isFinite(number) && number >= min && number <= max;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String join(List<String> values) {
        return String.join(",", values);
    }

    private List<String> split(String value) {
        if (value == null || value.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(value.split(",")));
    }

    private static Set<String> setOf(String... values) {
        return Collections.unmodifiableSet(new HashSet<>(Arrays.asList(values)));
    }
}
