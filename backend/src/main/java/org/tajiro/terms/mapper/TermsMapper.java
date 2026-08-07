package org.tajiro.terms.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.tajiro.terms.domain.TermsVO;

import java.util.List;

@Mapper
public interface TermsMapper {

    // 1. 현재 유효한(effective_at이 지난) 약관 목록 조회
    List<TermsVO> findEffectiveTerms();

    // 2. 약관 상세 조회
    TermsVO findById(@Param("termsId") Long termsId);
}
