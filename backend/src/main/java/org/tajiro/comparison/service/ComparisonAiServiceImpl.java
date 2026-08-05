package org.tajiro.comparison.service;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.tajiro.common.api.ErrorCode;
import org.tajiro.comparison.dto.ComparisonAnalysisRequestDTO;
import org.tajiro.comparison.dto.ComparisonAnalysisResponseDTO;
import org.tajiro.comparison.dto.ComparisonMetricDTO;
import org.tajiro.comparison.dto.ComparisonMetricsResponseDTO;
import org.tajiro.comparison.mapper.ComparisonMapper;
import org.tajiro.comparison.service.ai.ComparisonAiClient;
import org.tajiro.exception.BusinessException;
import org.tajiro.report.domain.ComparisonReportVO;
import org.tajiro.report.mapper.ComparisonReportMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ComparisonAiServiceImpl implements ComparisonAiService {

    private final ComparisonService comparisonService;
    private final ComparisonMapper comparisonMapper;
    private final ComparisonReportMapper comparisonReportMapper;
    private final ComparisonAiClient comparisonAiClient;

    @Override
    public ComparisonAnalysisResponseDTO analyze(
            Long userId,
            ComparisonAnalysisRequestDTO request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        List<Long> propertyIds = request.getPropertyIds();
        ComparisonMetricsResponseDTO metrics = comparisonService.getComparisonMetrics(
                userId,
                propertyIds);
        List<String> priorities = comparisonMapper.findPreferencePriorities(userId);

        String propertyIdsJson = toLongJson(propertyIds);
        String prioritiesJson = toStringJson(priorities);
        ComparisonReportVO reusableReport = comparisonReportMapper.findReusable(
                userId,
                propertyIdsJson,
                propertyIds.size(),
                prioritiesJson);

        if (isSuccessfulAiCoaching(reusableReport, propertyIds)) {
            return toAnalysisResponse(reusableReport);
        }

        ComparisonAnalysisResponseDTO generated = comparisonAiClient.generate(
                metrics.getItems(),
                priorities);
        ComparisonReportVO reportToRefresh = reusableReport;
        if (reportToRefresh == null) {
            ComparisonReportVO latestReport = comparisonReportMapper.findLatestBySameProperties(
                    userId,
                    propertyIdsJson,
                    propertyIds.size());
            if (!isSuccessfulAiCoaching(latestReport, propertyIds)) {
                reportToRefresh = latestReport;
            }
        }

        ComparisonReportVO report = reportToRefresh == null
                ? createReport(userId, propertyIdsJson, prioritiesJson, metrics.getItems(), generated)
                : refreshReport(reportToRefresh, prioritiesJson, metrics.getItems(), generated);

        generated.setReportId(report.getReportId());
        return generated;
    }

    private ComparisonReportVO createReport(
            Long userId,
            String propertyIdsJson,
            String prioritiesJson,
            List<ComparisonMetricDTO> items,
            ComparisonAnalysisResponseDTO generated) {
        ComparisonReportVO report = ComparisonReportVO.builder()
                .userId(userId)
                .title(buildTitle(items))
                .comparedPropertyIdsJson(propertyIdsJson)
                .preferencePrioritiesJson(prioritiesJson)
                .aiPropertySummaryText(generated.getAiPropertySummaryText())
                .aiSummary(generated.getAiSummary())
                .aiRecommendedPropertyId(generated.getAiRecommendedPropertyId())
                .saved(false)
                .aiAtp(generated.getAiAtp())
                .build();
        comparisonReportMapper.insert(report);
        return report;
    }

    private ComparisonReportVO refreshReport(
            ComparisonReportVO report,
            String prioritiesJson,
            List<ComparisonMetricDTO> items,
            ComparisonAnalysisResponseDTO generated) {
        report.setTitle(buildTitle(items));
        report.setPreferencePrioritiesJson(prioritiesJson);
        report.setAiPropertySummaryText(generated.getAiPropertySummaryText());
        report.setAiSummary(generated.getAiSummary());
        report.setAiRecommendedPropertyId(generated.getAiRecommendedPropertyId());
        report.setAiAtp(generated.getAiAtp());
        comparisonReportMapper.updateGeneratedReport(report);
        return report;
    }

    private ComparisonAnalysisResponseDTO toAnalysisResponse(ComparisonReportVO report) {
        return ComparisonAnalysisResponseDTO.builder()
                .reportId(report.getReportId())
                .aiPropertySummaryText(report.getAiPropertySummaryText())
                .aiSummary(report.getAiSummary())
                .aiRecommendedPropertyId(report.getAiRecommendedPropertyId())
                .aiAtp(report.getAiAtp())
                .build();
    }

    private boolean isSuccessfulAiCoaching(
            ComparisonReportVO report,
            List<Long> propertyIds) {
        return report != null
                && hasText(report.getAiPropertySummaryText())
                && hasText(report.getAiSummary())
                && hasText(report.getAiAtp())
                && report.getAiRecommendedPropertyId() != null
                && propertyIds.contains(report.getAiRecommendedPropertyId());
    }

    private String buildTitle(List<ComparisonMetricDTO> items) {
        if (items == null || items.isEmpty()) {
            return "비교 리포트";
        }

        String firstTitle = hasText(items.get(0).getTitle())
                ? items.get(0).getTitle().trim()
                : "비교 리포트";
        int additionalCount = Math.max(items.size() - 1, 0);
        return additionalCount == 0
                ? firstTitle
                : firstTitle + " 외 " + additionalCount + "건";
    }

    private String toLongJson(List<Long> values) {
        ArrayNode array = JsonNodeFactory.instance.arrayNode();
        values.forEach(array::add);
        return array.toString();
    }

    private String toStringJson(List<String> values) {
        ArrayNode array = JsonNodeFactory.instance.arrayNode();
        if (values != null) {
            values.forEach(array::add);
        }
        return array.toString();
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
