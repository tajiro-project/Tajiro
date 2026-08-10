package org.tajiro.report.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tajiro.common.api.ErrorCode;
import org.tajiro.exception.BusinessException;
import org.tajiro.report.domain.ComparisonReportVO;
import org.tajiro.report.dto.ComparisonReportCreateRequest;
import org.tajiro.report.dto.ComparisonReportResponse;
import org.tajiro.report.mapper.ComparisonReportMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComparisonReportServiceImpl implements ComparisonReportService {

    private static final String COMPARISON_WORKPLACE_PREFIX = "__COMPARE_WORKPLACE__:";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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
        Double[] workplace = extractComparisonWorkplace(
                report.getPreferencePrioritiesJson());
        return ComparisonReportResponse.builder()
                .reportId(report.getReportId())
                .title(report.getTitle())
                .comparedPropertyIds(comparisonReportMapper.findPropertyIdsByJson(propertyIdsJson))
                .comparedProperties(comparisonReportMapper.findPropertiesByJson(propertyIdsJson))
                .aiPropertySummaryText(report.getAiPropertySummaryText())
                .aiSummary(report.getAiSummary())
                .aiRecommendedPropertyId(report.getAiRecommendedPropertyId())
                .aiAtp(report.getAiAtp())
                .workplaceLat(workplace[0])
                .workplaceLng(workplace[1])
                .priorities(extractComparisonPriorities(
                        report.getPreferencePrioritiesJson()))
                .createdAt(report.getCreatedAt())
                .build();
    }

    private Double[] extractComparisonWorkplace(String contextJson) {
        Double[] workplace = new Double[]{null, null};
        if (contextJson == null || contextJson.trim().isEmpty()) {
            return workplace;
        }

        try {
            JsonNode context = OBJECT_MAPPER.readTree(contextJson);
            if (!context.isArray()) {
                return workplace;
            }

            for (JsonNode value : context) {
                String text = value.asText("");
                if (!text.startsWith(COMPARISON_WORKPLACE_PREFIX)) {
                    continue;
                }

                String[] coordinates = text
                        .substring(COMPARISON_WORKPLACE_PREFIX.length())
                        .split(",", 2);
                if (coordinates.length == 2) {
                    workplace[0] = Double.valueOf(coordinates[0]);
                    workplace[1] = Double.valueOf(coordinates[1]);
                }
                break;
            }
        } catch (Exception ignored) {
            // 이전 형식의 리포트는 위치 좌표 없이 반환한다.
        }
        return workplace;
    }

    private List<String> extractComparisonPriorities(String contextJson) {
        if (contextJson == null || contextJson.trim().isEmpty()) {
            return Collections.emptyList();
        }

        try {
            JsonNode context = OBJECT_MAPPER.readTree(contextJson);
            if (!context.isArray()) {
                return Collections.emptyList();
            }

            List<String> priorities = new ArrayList<>();
            for (JsonNode value : context) {
                String text = value.asText("");
                if (!text.startsWith(COMPARISON_WORKPLACE_PREFIX)) {
                    priorities.add(text);
                }
            }
            return priorities;
        } catch (Exception ignored) {
            return Collections.emptyList();
        }
    }
}
