package org.tajiro.comparison.mapper;

import org.apache.ibatis.annotations.Param;
import org.tajiro.comparison.dto.ComparePropertyDTO;
import org.tajiro.comparison.dto.ComparisonMetricDTO;

import java.util.List;

public interface ComparisonMapper {

    List<ComparePropertyDTO> findByUserId(@Param("userId") Long userId);

    boolean existsProperty(@Param("propertyId") Long propertyId);

    boolean existsByUserIdAndPropertyId(@Param("userId") Long userId,
                                        @Param("propertyId") Long propertyId);

    int countByUserId(@Param("userId") Long userId);

    int insert(@Param("userId") Long userId, @Param("propertyId") Long propertyId);

    int delete(@Param("userId") Long userId, @Param("propertyId") Long propertyId);

    List<ComparisonMetricDTO> findMetrics(@Param("userId") Long userId,
                                          @Param("propertyIds") List<Long> propertyIds);

    List<String> findPreferencePriorities(@Param("userId") Long userId);
}
