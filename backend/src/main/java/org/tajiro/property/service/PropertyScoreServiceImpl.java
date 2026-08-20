package org.tajiro.property.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PropertyScoreServiceImpl implements PropertyScoreService {

    private static final String COMMUTE = "COMMUTE";
    private static final String COST = "COST";
    private static final String INFRA = "INFRA";
    private static final String AMENITY = "AMENITY";
    private static final String AREA = "AREA";
    private static final String ALL_INFRA_CATEGORIES = String.join(",",
            "SUBWAY", "BUS_TERMINAL", "TRAIN", "HOSPITAL", "PHARMACY", "SCHOOL",
            "KINDERGARTEN", "ACADEMY", "LIBRARY", "PARK", "POLICE", "FIRE",
            "GOV_OFFICE", "PUBLIC", "POST_OFFICE", "BANK");
    private static final String ALL_AMENITY_CATEGORIES = String.join(",",
            "CONVENIENCE", "MART", "CAFE", "FOOD", "CULTURE", "SPORTS",
            "SWIMMING", "PARKING", "GAS");
    private static final List<String> DEFAULT_CRITERIA = List.of(
            COMMUTE, COST, INFRA, AMENITY, AREA);
    private static final Set<String> SUPPORTED_CRITERIA = Set.copyOf(DEFAULT_CRITERIA);

    private static final double MONTHLY_DEPOSIT_WEIGHT = 0.4;
    private static final double MONTHLY_RENT_WEIGHT = 0.6;

    private final PreferenceMapper preferenceMapper;
    private final PropertyMapper propertyMapper;
    private final PropertyScoreMapper propertyScoreMapper;

    @Override
    @Transactional
    public Map<Long, PropertyValueAnalysisResultVO> recalculateAllScores(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        HousingPreferenceVO preference = findPreference(userId);
        List<PropertyVO> properties = propertyMapper.getList(PropertySearchRequest.builder()
                .userId(userId)
                .refLat(preference.getWorkplaceLatitude())
                .refLng(preference.getWorkplaceLongitude())
                .desiredInfraCategories(preference.getDesiredInfraCategories())
                .desiredAmenityCategories(preference.getDesiredAmenityCategories())
                .applyDesiredCategoryFilter(false)
                .useAllCategoriesWhenEmpty(true)
                .build());

        return saveScores(userId, properties);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, PropertyComparisonScoreDTO> calculateComparisonScores(
            Long userId,
            List<Long> propertyIds,
            BigDecimal workplaceLatitude,
            BigDecimal workplaceLongitude) {
        if (userId == null
                || propertyIds == null
                || propertyIds.isEmpty()
                || propertyIds.stream().anyMatch(id -> id == null)
                || (workplaceLatitude == null) != (workplaceLongitude == null)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        HousingPreferenceVO preference = findPreference(userId);
        BigDecimal refLat = workplaceLatitude == null
                ? preference.getWorkplaceLatitude()
                : workplaceLatitude;
        BigDecimal refLng = workplaceLongitude == null
                ? preference.getWorkplaceLongitude()
                : workplaceLongitude;
        if (refLat == null || refLng == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        List<Long> distinctPropertyIds = propertyIds.stream()
        .distinct()
        .collect(Collectors.toList());
        List<PropertyVO> properties = propertyMapper.getList(PropertySearchRequest.builder()
                .userId(userId)
                .propertyIds(distinctPropertyIds)
                .refLat(refLat)
                .refLng(refLng)
                .desiredInfraCategories(preference.getDesiredInfraCategories())
                .desiredAmenityCategories(preference.getDesiredAmenityCategories())
                .applyDesiredCategoryFilter(false)
                .useAllCategoriesWhenEmpty(true)
                .build());

        if (properties.size() != distinctPropertyIds.size()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        return calculateComparisonScoreDetails(userId, preference, properties);
    }

    @Override
    @Transactional
    public Map<Long, PropertyValueAnalysisResultVO> saveScores(
            Long userId,
            List<PropertyVO> properties) {
        if (userId == null || properties == null || properties.stream()
                .anyMatch(property -> property == null || property.getId() == null)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (properties.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, PropertyValueAnalysisResultVO> scoresByPropertyId =
                calculateScores(userId, properties);

        propertyScoreMapper.upsertAll(List.copyOf(scoresByPropertyId.values()));
        return scoresByPropertyId;
    }

    private Map<Long, PropertyValueAnalysisResultVO> calculateScores(
            Long userId,
            List<PropertyVO> properties) {
        HousingPreferenceVO preference = findPreference(userId);
        Map<String, Double> criterionWeights = calculateRankSumWeights(
                preferenceMapper.findPrioritiesByUserId(userId));
        LocalDateTime computedAt = LocalDateTime.now();
        Map<Long, PropertyValueAnalysisResultVO> scoresByPropertyId = new LinkedHashMap<>();

        for (PropertyVO property : properties) {
            Map<String, Double> criterionScores =
                    calculateCriterionScores(property, preference);
            int recommendScore = calculateRecommendScore(
                    criterionScores, criterionWeights);
            PropertyValueAnalysisResultVO score = PropertyValueAnalysisResultVO.builder()
                    .userId(userId)
                    .propertyId(property.getId())
                    .workplaceDistanceMeters(property.getDistanceMeters())
                    .recommendScore(recommendScore)
                    .computedAt(computedAt)
                    .build();
            scoresByPropertyId.put(property.getId(), score);
        }
        return scoresByPropertyId;
    }

    private Map<Long, PropertyComparisonScoreDTO> calculateComparisonScoreDetails(
            Long userId,
            HousingPreferenceVO preference,
            List<PropertyVO> properties) {
        Map<String, Double> criterionWeights = calculateRankSumWeights(
                preferenceMapper.findPrioritiesByUserId(userId));
        Map<Long, PropertyComparisonScoreDTO> scoresByPropertyId =
                new LinkedHashMap<>();

        for (PropertyVO property : properties) {
            Map<String, Double> criterionScores =
                    calculateCriterionScores(property, preference);
            PropertyComparisonScoreDTO score = PropertyComparisonScoreDTO.builder()
                    .propertyId(property.getId())
                    .preferenceScore(calculateRecommendScore(
                            criterionScores, criterionWeights))
                    .commuteScore(roundScore(criterionScores.get(COMMUTE)))
                    .costScore(roundScore(criterionScores.get(COST)))
                    .infraScore(roundScore(criterionScores.get(INFRA)))
                    .amenityScore(roundScore(criterionScores.get(AMENITY)))
                    .build();
            scoresByPropertyId.put(property.getId(), score);
        }
        return scoresByPropertyId;
    }

    private Map<String, Double> calculateRankSumWeights(
            List<PreferencePriorityVO> priorities) {
        if (priorities == null || priorities.size() > 3) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        if (priorities.isEmpty()) {
            Map<String, Double> equalWeights = new LinkedHashMap<>();
            double weight = 1.0 / DEFAULT_CRITERIA.size();
            DEFAULT_CRITERIA.forEach(criterion -> equalWeights.put(criterion, weight));
            return equalWeights;
        }

        int criterionCount = priorities.size();
        double denominator = criterionCount * (criterionCount + 1) / 2.0;
        Set<String> criteria = new HashSet<>();
        Set<Integer> orders = new HashSet<>();
        Map<String, Double> weights = new LinkedHashMap<>();

        for (PreferencePriorityVO priority : priorities) {
            if (priority == null
                    || !SUPPORTED_CRITERIA.contains(priority.getCriterion())
                    || priority.getPriorityOrder() == null
                    || priority.getPriorityOrder() < 1
                    || priority.getPriorityOrder() > criterionCount
                    || !criteria.add(priority.getCriterion())
                    || !orders.add(priority.getPriorityOrder())) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
            }

            double weight = (criterionCount - priority.getPriorityOrder() + 1)
                    / denominator;
            weights.put(priority.getCriterion(), weight);
        }
        return weights;
    }

    private int calculateRecommendScore(
            Map<String, Double> criterionScores,
            Map<String, Double> criterionWeights) {
        double weightedScore = 0.0;

        for (Map.Entry<String, Double> entry : criterionWeights.entrySet()) {
            double criterionScore = criterionScores.get(entry.getKey());
            weightedScore += criterionScore * entry.getValue();
        }

        return roundScore(weightedScore);
    }

    private Map<String, Double> calculateCriterionScores(
            PropertyVO property,
            HousingPreferenceVO preference) {
        Map<String, Double> scores = new LinkedHashMap<>();
        for (String criterion : DEFAULT_CRITERIA) {
            scores.put(
                    criterion,
                    calculateCriterionScore(criterion, property, preference));
        }
        return scores;
    }

    private int roundScore(Double score) {
        return (int) Math.round(clamp(score == null ? 0.0 : score, 0.0, 100.0));
    }

    private double calculateCriterionScore(
            String criterion,
            PropertyVO property,
            HousingPreferenceVO preference) {
        switch (criterion) {
            case COMMUTE:
                return lowerIsBetter(
                        property.getDistanceMeters(),
                        0,
                        preference.getMaxWorkplaceDistanceMeters());
            case COST:
                return calculateCostScore(property, preference);
            case INFRA:
                return calculateCoverageScore(
                        property.getDesiredInfraCount(),
                        categoriesOrAll(
                                preference.getDesiredInfraCategories(),
                                ALL_INFRA_CATEGORIES));
            case AMENITY:
                return calculateCoverageScore(
                        property.getDesiredAmenityCount(),
                        categoriesOrAll(
                                preference.getDesiredAmenityCategories(),
                                ALL_AMENITY_CATEGORIES));
            case AREA:
                return higherIsBetter(
                        property.getAreaM2(),
                        preference.getMinArea(),
                        preference.getMaxArea());
            default:
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private double calculateCostScore(
            PropertyVO property,
            HousingPreferenceVO preference) {
        if (property.getTradeType() == null || property.getDeposit() == null) {
            return 0.0;
        }

        switch (property.getTradeType()) {
            case "월세": {
                double depositScore = lowerIsBetter(
                        property.getDeposit(),
                        preference.getMinDeposit(),
                        preference.getMaxDeposit());

                double rentScore = lowerIsBetter(
                        property.getMonthlyRent(),
                        preference.getMinMonthlyRent(),
                        preference.getMaxMonthlyRent());
                return depositScore * MONTHLY_DEPOSIT_WEIGHT
                        + rentScore * MONTHLY_RENT_WEIGHT;
            }
            case "전세":
                return lowerIsBetter(
                        property.getDeposit(),
                        preference.getMinDeposit(),
                        preference.getMaxDeposit());
            case "매매":
                return lowerIsBetter(
                        property.getDeposit(),
                        preference.getMinSellingPrice(),
                        preference.getMaxSellingPrice());
            default:
                return 0.0;
        }
    }

    private double calculateCoverageScore(Integer matchedCount, String categoriesCsv) {
        int categoryCount = countCategories(categoriesCsv);
        if (categoryCount == 0) {
            return 0.0;
        }
        int safeMatchedCount = matchedCount == null ? 0 : matchedCount;
        return clamp(safeMatchedCount * 100.0 / categoryCount, 0.0, 100.0);
    }

    private int countCategories(String categoriesCsv) {
        if (categoriesCsv == null || categoriesCsv.trim().isEmpty()) {
            return 0;
        }
        return (int) Arrays.stream(categoriesCsv.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .distinct()
                .count();
    }

    private String categoriesOrAll(String selectedCategories, String allCategories) {
        return selectedCategories == null || selectedCategories.trim().isEmpty()
                ? allCategories
                : selectedCategories;
    }

    private HousingPreferenceVO findPreference(Long userId) {
        HousingPreferenceVO preference = preferenceMapper.findByUserId(userId);
        if (preference == null) {
            throw new BusinessException(ErrorCode.PREFERENCE_NOT_FOUND);
        }
        return preference;
    }

    private double lowerIsBetter(Number value, Number min, Number max) {
        if (value == null || min == null || max == null) {
            return 0.0;
        }
        return normalize(value.doubleValue(), min.doubleValue(), max.doubleValue(), true);
    }

    private double higherIsBetter(BigDecimal value, BigDecimal min, BigDecimal max) {
        if (value == null || min == null || max == null) {
            return 0.0;
        }
        return normalize(value.doubleValue(), min.doubleValue(), max.doubleValue(), false);
    }

    private double normalize(double value, double min, double max, boolean lowerIsBetter) {
        if (!Double.isFinite(value)
                || !Double.isFinite(min)
                || !Double.isFinite(max)
                || min > max) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (Double.compare(min, max) == 0) {
            return Double.compare(value, min) == 0 ? 100.0 : 0.0;
        }

        double normalized = (value - min) / (max - min);
        double score = (lowerIsBetter ? 1.0 - normalized : normalized) * 100.0;
        return clamp(score, 0.0, 100.0);
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
