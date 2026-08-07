package org.tajiro.property.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tajiro.common.api.ErrorCode;
import org.tajiro.exception.BusinessException;
import org.tajiro.preference.domain.HousingPreferenceVO;
import org.tajiro.preference.domain.PreferencePriorityVO;
import org.tajiro.preference.mapper.PreferenceMapper;
import org.tajiro.property.domain.PropertyVO;
import org.tajiro.property.domain.PropertyValueAnalysisResultVO;
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

@Service
@RequiredArgsConstructor
public class PropertyScoreServiceImpl implements PropertyScoreService {

    private static final String COMMUTE = "COMMUTE";
    private static final String COST = "COST";
    private static final String INFRA = "INFRA";
    private static final String AMENITY = "AMENITY";
    private static final String AREA = "AREA";
    private static final Set<String> SUPPORTED_CRITERIA = Set.of(
            COMMUTE, COST, INFRA, AMENITY, AREA);

    private static final double MONTHLY_DEPOSIT_WEIGHT = 0.4;
    private static final double MONTHLY_RENT_WEIGHT = 0.6;

    private final PreferenceMapper preferenceMapper;
    private final PropertyScoreMapper propertyScoreMapper;

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

        HousingPreferenceVO preference = preferenceMapper.findByUserId(userId);
        if (preference == null) {
            throw new BusinessException(ErrorCode.PREFERENCE_NOT_FOUND);
        }

        Map<String, Double> criterionWeights = calculateRankSumWeights(
                preferenceMapper.findPrioritiesByUserId(userId));
        LocalDateTime computedAt = LocalDateTime.now();
        Map<Long, PropertyValueAnalysisResultVO> scoresByPropertyId = new LinkedHashMap<>();

        for (PropertyVO property : properties) {
            int recommendScore = calculateRecommendScore(
                    property, preference, criterionWeights);
            PropertyValueAnalysisResultVO score = PropertyValueAnalysisResultVO.builder()
                    .userId(userId)
                    .propertyId(property.getId())
                    .workplaceDistanceMeters(property.getDistanceMeters())
                    .recommendScore(recommendScore)
                    .computedAt(computedAt)
                    .build();
            scoresByPropertyId.put(property.getId(), score);
        }

        propertyScoreMapper.upsertAll(List.copyOf(scoresByPropertyId.values()));
        return scoresByPropertyId;
    }

    private Map<String, Double> calculateRankSumWeights(
            List<PreferencePriorityVO> priorities) {
        if (priorities == null || priorities.isEmpty() || priorities.size() > 3) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
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
            PropertyVO property,
            HousingPreferenceVO preference,
            Map<String, Double> criterionWeights) {
        double weightedScore = 0.0;

        for (Map.Entry<String, Double> entry : criterionWeights.entrySet()) {
            double criterionScore = calculateCriterionScore(
                    entry.getKey(), property, preference);
            weightedScore += criterionScore * entry.getValue();
        }

        return (int) Math.round(clamp(weightedScore, 0.0, 100.0));
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
                        preference.getDesiredInfraCategories());
            case AMENITY:
                return calculateCoverageScore(
                        property.getDesiredAmenityCount(),
                        preference.getDesiredAmenityCategories());
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
