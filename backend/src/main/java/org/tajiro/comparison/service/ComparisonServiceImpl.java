package org.tajiro.comparison.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.tajiro.comparison.dto.ComparePropertyDTO;
import org.tajiro.comparison.dto.ComparisonMetricDTO;
import org.tajiro.comparison.dto.ComparisonMetricsResponseDTO;
import org.tajiro.comparison.mapper.ComparisonMapper;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ComparisonServiceImpl implements ComparisonService {

    private static final int MAX_COMPARE_PROPERTIES = 3;
    private static final int MIN_COMPARISON_PROPERTIES = 2;

    private final ComparisonMapper comparisonMapper;

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
    @Transactional(readOnly = true)
    public ComparisonMetricsResponseDTO getComparisonMetrics(Long userId, List<Long> propertyIds) {
        if (propertyIds == null
                || propertyIds.size() < MIN_COMPARISON_PROPERTIES
                || propertyIds.size() > MAX_COMPARE_PROPERTIES
                || new HashSet<>(propertyIds).size() != propertyIds.size()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "비교할 매물 ID는 서로 다른 값으로 2개 이상 3개 이하만 요청할 수 있습니다.");
        }
        //Mapper를 통해 DB에서 매물 ID에 해당하는 ComparisonMetricDTO를 조회한다.
        List<ComparisonMetricDTO> items = comparisonMapper.findMetrics(userId, propertyIds);

        if (items.size() != propertyIds.size()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 매물이 포함되어 있습니다.");
        }

        return ComparisonMetricsResponseDTO.builder()
                .items(items)
                .build();
    }
}
