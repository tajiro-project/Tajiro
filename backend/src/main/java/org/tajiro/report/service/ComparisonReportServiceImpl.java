package org.tajiro.report.service;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tajiro.common.api.ErrorCode;
import org.tajiro.exception.BusinessException;
import org.tajiro.report.domain.ComparisonReportVO;
import org.tajiro.report.dto.ComparisonReportCreateRequest;
import org.tajiro.report.dto.ComparisonReportResponse;
import org.tajiro.report.mapper.ComparisonReportMapper;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComparisonReportServiceImpl implements ComparisonReportService {

    private static final int MIN_PROPERTIES = 2;
    private static final int MAX_PROPERTIES = 3;

    private final ComparisonReportMapper comparisonReportMapper;

    @Override
    @Transactional
    public ComparisonReportResponse create(Long userId, ComparisonReportCreateRequest request) {
        validateRequest(request);

        List<Long> propertyIds = request.getComparedPropertyIds();
        if (comparisonReportMapper.countExistingProperties(propertyIds) != propertyIds.size()) {
            throw new BusinessException(ErrorCode.PROPERTY_NOT_FOUND);
        }

        String propertyIdsJson = toJson(propertyIds);
        ComparisonReportVO existingReport = comparisonReportMapper.findSavedBySameProperties(
                userId,
                propertyIdsJson,
                propertyIds.size());
        if (existingReport != null) {
            return toResponse(existingReport);
        }

        ComparisonReportVO report = ComparisonReportVO.builder()
                .userId(userId)
                .title(request.getTitle().trim())
                .comparedPropertyIdsJson(propertyIdsJson)
                .aiPropertySummaryText(defaultText(request.getAiPropertySummaryText()))
                .aiSummary(defaultText(request.getAiSummary()))
                .aiRecommendedPropertyId(request.getAiRecommendedPropertyId())
                .aiAtp(defaultText(request.getAiAtp()))
                .build();

        comparisonReportMapper.insert(report);
        return get(userId, report.getReportId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComparisonReportResponse> getAll(Long userId) {
        return comparisonReportMapper.findAllByUserId(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ComparisonReportResponse get(Long userId, Long reportId) {
        ComparisonReportVO report = comparisonReportMapper.findByIdAndUserId(reportId, userId);
        if (report == null) {
            throw new BusinessException(ErrorCode.COMPARISON_REPORT_NOT_FOUND);
        }
        return toResponse(report);
    }

    @Override
    @Transactional
    public void delete(Long userId, Long reportId) {
        if (comparisonReportMapper.deleteByIdAndUserId(reportId, userId) == 0) {
            throw new BusinessException(ErrorCode.COMPARISON_REPORT_NOT_FOUND);
        }
    }

    private void validateRequest(ComparisonReportCreateRequest request) {
        if (request == null || request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        List<Long> propertyIds = request.getComparedPropertyIds();
        if (propertyIds == null
                || propertyIds.size() < MIN_PROPERTIES
                || propertyIds.size() > MAX_PROPERTIES
                || propertyIds.stream().anyMatch(id -> id == null)
                || new HashSet<>(propertyIds).size() != propertyIds.size()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        Long recommendedPropertyId = request.getAiRecommendedPropertyId();
        if (recommendedPropertyId != null && !propertyIds.contains(recommendedPropertyId)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private ComparisonReportResponse toResponse(ComparisonReportVO report) {
        String propertyIdsJson = report.getComparedPropertyIdsJson();
        return ComparisonReportResponse.builder()
                .reportId(report.getReportId())
                .title(report.getTitle())
                .comparedPropertyIds(comparisonReportMapper.findPropertyIdsByJson(propertyIdsJson))
                .comparedProperties(comparisonReportMapper.findPropertiesByJson(propertyIdsJson))
                .aiPropertySummaryText(report.getAiPropertySummaryText())
                .aiSummary(report.getAiSummary())
                .aiRecommendedPropertyId(report.getAiRecommendedPropertyId())
                .aiAtp(report.getAiAtp())
                .createdAt(report.getCreatedAt())
                .build();
    }

    private String toJson(List<Long> propertyIds) {
        ArrayNode array = JsonNodeFactory.instance.arrayNode();
        propertyIds.forEach(array::add);
        return array.toString();
    }

    private String defaultText(String value) {
        return value == null ? "" : value;
    }
}
