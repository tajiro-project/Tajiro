package org.tajiro.report.mapper;

import org.apache.ibatis.annotations.Param;
import org.tajiro.report.domain.ComparisonReportVO;
import org.tajiro.report.dto.ComparisonReportPropertyDTO;

import java.util.List;

public interface ComparisonReportMapper {
    int countExistingProperties(@Param("propertyIds") List<Long> propertyIds);

    int insert(ComparisonReportVO report);

    List<ComparisonReportVO> findAllByUserId(@Param("userId") Long userId);

    ComparisonReportVO findSavedBySameProperties(@Param("userId") Long userId,
                                                 @Param("propertyIdsJson") String propertyIdsJson,
                                                 @Param("propertyCount") int propertyCount);

    ComparisonReportVO findByIdAndUserId(@Param("reportId") Long reportId,
                                         @Param("userId") Long userId);

    List<Long> findPropertyIdsByJson(@Param("propertyIdsJson") String propertyIdsJson);

    List<ComparisonReportPropertyDTO> findPropertiesByJson(
            @Param("propertyIdsJson") String propertyIdsJson);

    int deleteByIdAndUserId(@Param("reportId") Long reportId,
                            @Param("userId") Long userId);
}
