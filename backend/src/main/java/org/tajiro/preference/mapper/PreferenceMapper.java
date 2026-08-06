package org.tajiro.preference.mapper;

import org.apache.ibatis.annotations.Param;
import org.tajiro.preference.domain.HousingPreferenceVO;
import org.tajiro.preference.domain.PreferencePriorityVO;

import java.util.List;

public interface PreferenceMapper {
    HousingPreferenceVO findByUserId(@Param("userId") Long userId);

    List<PreferencePriorityVO> findPrioritiesByUserId(@Param("userId") Long userId);

    int upsert(HousingPreferenceVO preference);

    int deletePrioritiesByUserId(@Param("userId") Long userId);

    int insertPriorities(@Param("priorities") List<PreferencePriorityVO> priorities);
}
