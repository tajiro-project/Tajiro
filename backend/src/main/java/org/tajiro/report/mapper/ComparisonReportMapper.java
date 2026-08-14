package org.tajiro.report.mapper;

import org.apache.ibatis.annotations.Param;
import org.tajiro.report.domain.ComparisonReportVO;
import org.tajiro.report.dto.ComparisonReportPropertyDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface ComparisonReportMapper {
    int insert(ComparisonReportVO report);

    List<ComparisonReportVO> findAllByUserId(@Param("userId") Long userId);

    ComparisonReportVO findReusable(@Param("userId") Long userId,
                                    @Param("propertyIdsJson") String propertyIdsJson,
                                    @Param("propertyCount") int propertyCount,
                                    @Param("preferencePrioritiesJson") String preferencePrioritiesJson);

    ComparisonReportVO findLatestBySameProperties(@Param("userId") Long userId,
                                                  @Param("propertyIdsJson") String propertyIdsJson,
                                                  @Param("propertyCount") int propertyCount);
    ComparisonReportVO findByIdAndUserId(@Param("reportId") Long reportId,
                                         @Param("userId") Long userId);

    int updateGeneratedReport(ComparisonReportVO report);

    int updateCreatedAt(@Param("reportId") Long reportId,
                       @Param("userId") Long userId);

    int markSaved(@Param("reportId") Long reportId,
                  @Param("userId") Long userId);

    List<Long> findPropertyIdsByJson(@Param("propertyIdsJson") String propertyIdsJson);

    List<ComparisonReportPropertyDTO> findPropertiesByJson(
            @Param("propertyIdsJson") String propertyIdsJson);

    LocalDateTime findLatestMarketSyncAtByJson(
            @Param("propertyIdsJson") String propertyIdsJson);

    int markUnsaved(@Param("reportId") Long reportId,
                    @Param("userId") Long userId);
}
