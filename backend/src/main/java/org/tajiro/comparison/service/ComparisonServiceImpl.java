package org.tajiro.comparison.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.tajiro.common.api.ErrorCode;
import org.tajiro.comparison.dto.ComparePropertyDTO;
import org.tajiro.comparison.dto.ComparisonMetricDTO;
import org.tajiro.comparison.dto.ComparisonMetricsResponseDTO;
import org.tajiro.comparison.mapper.ComparisonMapper;
import org.tajiro.exception.BusinessException;
import org.tajiro.market.service.MarketEvaluationService;
import org.tajiro.property.dto.PropertyComparisonScoreDTO;
import org.tajiro.property.service.PropertyScoreService;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ComparisonServiceImpl implements ComparisonService {

    private static final int MAX_COMPARE_PROPERTIES = 3;
    private static final int MIN_COMPARISON_PROPERTIES = 2;

    private final ComparisonMapper comparisonMapper;
    private final MarketEvaluationService marketEvaluationService;
    private final PropertyScoreService propertyScoreService;

    @Override
    @Transactional(readOnly = true)
    public List<ComparePropertyDTO> getCompareProperties(Long userId) {
        return comparisonMapper.findByUserId(userId);
    }

    @Override
    @Transactional
    public void addCompareProperty(Long userId, Long propertyId) {
        if (!comparisonMapper.existsProperty(propertyId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 매물입니다.");
        }

        if (comparisonMapper.existsByUserIdAndPropertyId(userId, propertyId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 비교함에 담긴 매물입니다.");
        }

        if (comparisonMapper.countByUserId(userId) >= MAX_COMPARE_PROPERTIES) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "비교함에는 매물을 최대 3개까지 담을 수 있습니다.");
        }

        try {
            comparisonMapper.insert(userId, propertyId);
        } catch (DuplicateKeyException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 비교함에 담긴 매물입니다.", e);
        }
    }

    @Override
    @Transactional
    public void removeCompareProperty(Long userId, Long propertyId) {
        comparisonMapper.delete(userId, propertyId);
    }

    //사용자 ID와 매물 ID 목록을 검증한 다음, DB에서 비교 지표를 조회하여 DTO로 반환
    @Override
    @Transactional
    public ComparisonMetricsResponseDTO getComparisonMetrics(
            Long userId,
            List<Long> propertyIds,
            Double workplaceLat,
            Double workplaceLng,
            boolean refreshMarketScore) {
        if (propertyIds == null
                || propertyIds.size() < MIN_COMPARISON_PROPERTIES
                || propertyIds.size() > MAX_COMPARE_PROPERTIES
                || propertyIds.stream().anyMatch(id -> id == null)
                || new HashSet<>(propertyIds).size() != propertyIds.size()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        validateWorkplace(workplaceLat, workplaceLng);

        // 외부 API를 호출하지 않고, 이미 수집된 실거래 데이터로 최신 시세 차이율을 계산한다.
        if (refreshMarketScore) {
            marketEvaluationService.evaluateProperties(propertyIds);
        }

        //Mapper를 통해 DB에서 매물 ID에 해당하는 ComparisonMetricDTO를 조회한다.
        List<ComparisonMetricDTO> items = comparisonMapper.findMetrics(
                userId,
                propertyIds,
                workplaceLat,
                workplaceLng);

        if (items.size() != propertyIds.size()) {
            throw new BusinessException(ErrorCode.PROPERTY_NOT_FOUND);
        }

        Map<Long, PropertyComparisonScoreDTO> scores =
                propertyScoreService.calculateComparisonScores(
                        userId,
                        propertyIds,
                        toBigDecimal(workplaceLat),
                        toBigDecimal(workplaceLng));
        for (ComparisonMetricDTO item : items) {
            PropertyComparisonScoreDTO score = scores.get(item.getPropertyId());
            if (score == null) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
            }
            item.setPreferenceScore(score.getPreferenceScore());
            item.setCommuteScore(score.getCommuteScore());
            item.setCostScore(score.getCostScore());
            item.setInfraScore(score.getInfraScore());
            item.setAmenityScore(score.getAmenityScore());
        }

        return ComparisonMetricsResponseDTO.builder()
                .items(items)
                .build();
    }

    private BigDecimal toBigDecimal(Double value) {
        return value == null ? null : BigDecimal.valueOf(value);
    }

    private void validateWorkplace(Double workplaceLat, Double workplaceLng) {
        if (workplaceLat == null && workplaceLng == null) {
            return;
        }

        if (workplaceLat == null
                || workplaceLng == null
                || !Double.isFinite(workplaceLat)
                || !Double.isFinite(workplaceLng)
                || workplaceLat < -90
                || workplaceLat > 90
                || workplaceLng < -180
                || workplaceLng > 180) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }
}
