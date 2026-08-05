package org.tajiro.report.service;

import org.tajiro.report.dto.ComparisonReportCreateRequest;
import org.tajiro.report.dto.ComparisonReportResponse;

import java.util.List;

public interface ComparisonReportService {
    ComparisonReportResponse create(Long userId, ComparisonReportCreateRequest request);
    //리포트 전체 조회
    List<ComparisonReportResponse> getAll(Long userId);
    //리포트 상세 조회
    ComparisonReportResponse get(Long userId, Long reportId);

    void delete(Long userId, Long reportId);
}
