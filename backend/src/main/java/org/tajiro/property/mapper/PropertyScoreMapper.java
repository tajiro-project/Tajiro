package org.tajiro.property.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.tajiro.property.domain.PropertyValueAnalysisResultVO;

import java.util.List;

@Mapper
public interface PropertyScoreMapper {
    void upsertAll(@Param("scores") List<PropertyValueAnalysisResultVO> scores);
}
