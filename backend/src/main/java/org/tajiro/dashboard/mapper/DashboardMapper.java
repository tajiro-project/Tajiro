package org.tajiro.dashboard.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.tajiro.dashboard.dto.DashboardDTO;

import java.util.List;

@Mapper
public interface DashboardMapper {
    String getUserName(@Param("userId") Long userId);

    List<DashboardDTO.PriorityItem> getPriorities(@Param("userId") Long userId);

    int countFavorites(@Param("userId") Long userId);

    int countReports(@Param("userId") Long userId);
}
