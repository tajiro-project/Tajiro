package org.tajiro.comparison.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.tajiro.comparison.dto.ComparePropertyDTO;
import org.tajiro.comparison.mapper.ComparisonMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ComparisonServiceImpl implements ComparisonService {

    private static final int MAX_COMPARE_PROPERTIES = 3;

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
}