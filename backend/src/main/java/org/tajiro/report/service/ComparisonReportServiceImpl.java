package org.tajiro.report.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tajiro.common.api.ErrorCode;
import org.tajiro.exception.BusinessException;
import org.tajiro.report.domain.ComparisonReportVO;
import org.tajiro.report.dto.ComparisonReportCreateRequest;
import org.tajiro.report.dto.ComparisonReportResponse;
import org.tajiro.report.mapper.ComparisonReportMapper;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComparisonReportServiceImpl implements ComparisonReportService {

    private final ComparisonReportMapper comparisonReportMapper;

    @Override
    @Transactional
    public ComparisonReportResponse create(Long userId, ComparisonReportCreateRequest request) {
        if (request == null || request.getReportId() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        comparisonReportMapper.markSaved(request.getReportId(), userId);
        return get(userId, request.getReportId());
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
        if (comparisonReportMapper.markUnsaved(reportId, userId) == 0) {
            throw new BusinessException(ErrorCode.COMPARISON_REPORT_NOT_FOUND);
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
}
