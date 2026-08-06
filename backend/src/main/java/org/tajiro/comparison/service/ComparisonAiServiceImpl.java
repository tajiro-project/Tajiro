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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
        if (request == null
                || request.getPropertyIds() == null
                || request.getPropertyIds().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        List<Long> propertyIds = request.getPropertyIds().stream()
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        //비교 지표 조회
        ComparisonMetricsResponseDTO metrics = comparisonService.getComparisonMetrics(
                userId,
                propertyIds);
        //사용자 가치관 우선순위 조회
        List<String> priorities = comparisonMapper.findPreferencePriorities(userId);
        //비교 대상 매물 중 최신 업데이트 날짜 추출
        LocalDateTime maxPropertyUpdateDate = getMaxPropertyUpdateDate(metrics.getItems());

        String propertyIdsJson = toLongJson(propertyIds);
        String prioritiesJson = toStringJson(priorities);
        //재사용 가능한 리포트 조회: 사용자, 비교 대상 매물, 사용자 가치관 우선순위가 동일한 리포트
        ComparisonReportVO reusableReport = comparisonReportMapper.findReusable(
                userId,
                propertyIdsJson,
                propertyIds.size(),
                prioritiesJson);
        //재사용 가능한 리포트가 존재하고, 기존 AI 코칭이 성공적이며, 매물 업데이트 이후에 생성되었으면 재사용
        if (isSuccessfulAiCoaching(reusableReport, propertyIds, maxPropertyUpdateDate)) {
            comparisonReportMapper.updateCreatedAt(reusableReport.getReportId(), userId);
            return toAnalysisResponse(reusableReport);
        }
        
        //새 AI 코칭 결과 생성
        ComparisonAnalysisResponseDTO generated = comparisonAiClient.generate(
                metrics.getItems(),
                priorities);
        //기존 리포트 있는지 확인
        ComparisonReportVO reportToRefresh = reusableReport;
        if (reportToRefresh == null) {
            ComparisonReportVO latestReport = comparisonReportMapper.findLatestBySameProperties(
                    userId,
                    propertyIdsJson,
                    propertyIds.size());
            if (!isSuccessfulAiCoaching(latestReport, propertyIds, maxPropertyUpdateDate)) {
                reportToRefresh = latestReport;
            }
        }

        //재사용할 게 없다면 새로 생성, 있다면 갱신
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
            List<Long> propertyIds,
            LocalDateTime maxPropertyUpdateDate) {
        if (report == null
                || !hasText(report.getAiPropertySummaryText())
                || !hasText(report.getAiSummary())
                || !hasText(report.getAiAtp())
                || report.getAiRecommendedPropertyId() == null
                || !propertyIds.contains(report.getAiRecommendedPropertyId())) {
            return false;
        }

        // 기존 리포트 생성일시(createdAt)보다 매물의 최신 수정일(maxPropertyUpdateDate)이 더 뒤라면 리포트 재사용 불가 (갱신 필요)
        if (maxPropertyUpdateDate != null && report.getCreatedAt() != null) {
            if (report.getCreatedAt().isBefore(maxPropertyUpdateDate)) {
                return false;
            }
        }

        return true;
    }

    private LocalDateTime getMaxPropertyUpdateDate(List<ComparisonMetricDTO> items) {
        if (items == null || items.isEmpty()) {
            return null;
        }
        return items.stream()
                .map(ComparisonMetricDTO::getUpdateDate)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);
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
