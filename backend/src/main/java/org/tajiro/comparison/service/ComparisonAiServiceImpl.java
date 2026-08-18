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
import org.tajiro.comparison.service.ai.ComparisonAiClient;
import org.tajiro.exception.BusinessException;
import org.tajiro.preference.domain.HousingPreferenceVO;
import org.tajiro.preference.domain.PreferencePriorityVO;
import org.tajiro.preference.mapper.PreferenceMapper;
import org.tajiro.report.domain.ComparisonReportVO;
import org.tajiro.report.mapper.ComparisonReportMapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComparisonAiServiceImpl implements ComparisonAiService {

    private static final String COMPARISON_WORKPLACE_PREFIX = "__COMPARE_WORKPLACE__:";
    private static final String SCORE_CONTEXT_PREFIX = "__PREFERENCE_SCORE_CONTEXT__:";
    private static final String SCORE_CONTEXT_VERSION = "preference-score-v1";
    private static final Set<String> SUPPORTED_PRIORITIES = Set.of(
            "COMMUTE",
            "COST",
            "INFRA",
            "AMENITY",
            "AREA");

    private final ComparisonService comparisonService;
    private final ComparisonReportMapper comparisonReportMapper;
    private final ComparisonAiClient comparisonAiClient;
    private final PreferenceMapper preferenceMapper;

    @Override
    public ComparisonAnalysisResponseDTO analyze(
            Long userId,
            ComparisonAnalysisRequestDTO request) {
        if (request == null
                || request.getPropertyIds() == null
                || request.getPropertyIds().isEmpty()
                || request.getWorkplaceLat() == null
                || request.getWorkplaceLng() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        List<Long> propertyIds = request.getPropertyIds().stream()
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        List<String> priorities = resolvePriorities(
                userId,
                request.getPriorities());
        HousingPreferenceVO preference = preferenceMapper.findByUserId(userId);
        if (preference == null) {
            throw new BusinessException(ErrorCode.PREFERENCE_NOT_FOUND);
        }

        String propertyIdsJson = toLongJson(propertyIds);
        LocalDateTime latestMarketSyncAt =
                comparisonReportMapper.findLatestMarketSyncAtByJson(propertyIdsJson);
        String prioritiesJson = toStringJson(buildComparisonContext(
                priorities,
                request.getWorkplaceLat(),
                request.getWorkplaceLng(),
                buildScoreContextHash(preference)));

        // 시세 점수를 새로 계산하지 않고 현재 DB 값만 조회해 기존 리포트 재사용 가능 여부를 먼저 판단한다.
        ComparisonMetricsResponseDTO metrics = comparisonService.getComparisonMetrics(
                userId,
                propertyIds,
                request.getWorkplaceLat(),
                request.getWorkplaceLng(),
                false);
        LocalDateTime maxPropertyUpdateDate = getMaxPropertyUpdateDate(metrics.getItems());

        //재사용 가능한 리포트 조회: 사용자, 비교 대상 매물, 사용자 가치관 우선순위가 동일한 리포트
        ComparisonReportVO reusableReport = comparisonReportMapper.findReusable(
                userId,
                propertyIdsJson,
                propertyIds.size(),
                prioritiesJson);
        //재사용 가능한 리포트가 존재하고, 기존 AI 코칭이 성공적이며, 매물/실거래 동기화 이후에 생성되었으면 재사용
        if (isSuccessfulAiCoaching(
                reusableReport,
                propertyIds,
                maxPropertyUpdateDate,
                latestMarketSyncAt)) {
            comparisonReportMapper.updateCreatedAt(reusableReport.getReportId(), userId);
            return toAnalysisResponse(reusableReport);
        }

        // 기존 리포트를 재사용할 수 없을 때만 최신 시세 차이율을 다시 계산한다.
        metrics = comparisonService.getComparisonMetrics(
                userId,
                propertyIds,
                request.getWorkplaceLat(),
                request.getWorkplaceLng(),
                true);
        maxPropertyUpdateDate = getMaxPropertyUpdateDate(metrics.getItems());

        // 비교 지표 조회 단계에서 계산된 매물별 맞춤 점수를 전달하고, 최종 추천은 AI가 판단한다.
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
            if (!isSuccessfulAiCoaching(
                    latestReport,
                    propertyIds,
                    maxPropertyUpdateDate,
                    latestMarketSyncAt)) {
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
            LocalDateTime maxPropertyUpdateDate,
            LocalDateTime latestMarketSyncAt) {
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

        if (latestMarketSyncAt != null && report.getCreatedAt() != null) {
            if (report.getCreatedAt().isBefore(latestMarketSyncAt)) {
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

    private List<String> buildComparisonContext(
            List<String> priorities,
            Double workplaceLat,
            Double workplaceLng,
            String scoreContextHash) {
        List<String> context = priorities == null
                ? new ArrayList<>()
                : new ArrayList<>(priorities);
        context.add(String.format(
                Locale.ROOT,
                "%s%.6f,%.6f",
                COMPARISON_WORKPLACE_PREFIX,
                workplaceLat,
                workplaceLng));
        context.add(SCORE_CONTEXT_PREFIX + scoreContextHash);
        return context;
    }

    private List<String> resolvePriorities(
            Long userId,
            List<String> requestedPriorities) {
        List<String> validatedRequest = validatePriorities(requestedPriorities);
        List<String> storedPriorities = preferenceMapper.findPrioritiesByUserId(userId)
                .stream()
                .sorted((left, right) -> left.getPriorityOrder()
                        .compareTo(right.getPriorityOrder()))
                .map(PreferencePriorityVO::getCriterion)
                .collect(Collectors.toList());
        validatePriorities(storedPriorities);

        if (!validatedRequest.isEmpty()
                && !validatedRequest.equals(storedPriorities)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        return storedPriorities;
    }

    private String buildScoreContextHash(HousingPreferenceVO preference) {
        String context = String.join("|",
                SCORE_CONTEXT_VERSION,
                valueOf(preference.getMinDeposit()),
                valueOf(preference.getMaxDeposit()),
                valueOf(preference.getMinMonthlyRent()),
                valueOf(preference.getMaxMonthlyRent()),
                valueOf(preference.getMinSellingPrice()),
                valueOf(preference.getMaxSellingPrice()),
                valueOf(preference.getMinArea()),
                valueOf(preference.getMaxArea()),
                valueOf(preference.getMaxWorkplaceDistanceMeters()),
                valueOf(preference.getDesiredInfraCategories()),
                valueOf(preference.getDesiredAmenityCategories()));
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(context.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    private String valueOf(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private List<String> validatePriorities(List<String> priorities) {
        if (priorities == null || priorities.isEmpty()) {
            return new ArrayList<>();
        }

        if (priorities.size() > 3) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        Set<String> seen = new HashSet<>();
        List<String> validated = new ArrayList<>();
        for (String priority : priorities) {
            if (priority == null
                    || !SUPPORTED_PRIORITIES.contains(priority)
                    || !seen.add(priority)) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
            }
            validated.add(priority);
        }
        return validated;
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
