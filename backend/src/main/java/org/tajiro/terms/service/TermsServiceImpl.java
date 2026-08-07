package org.tajiro.terms.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.tajiro.common.api.ErrorCode;
import org.tajiro.exception.BusinessException;
import org.tajiro.terms.domain.TermsVO;
import org.tajiro.terms.dto.TermsDTO;
import org.tajiro.terms.dto.TermsDetailDTO;
import org.tajiro.terms.mapper.TermsMapper;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TermsServiceImpl implements TermsService {

    private final TermsMapper termsMapper;

    @Override
    public List<TermsDTO> getEffectiveTerms() {
        return termsMapper.findEffectiveTerms().stream()
                .map(vo -> TermsDTO.builder()
                        .id(vo.getId())
                        .title(vo.getTitle())
                        .required(vo.getRequired())
                        .version(vo.getVersion())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public TermsDetailDTO getTermsDetail(Long termsId) {
        TermsVO vo = termsMapper.findById(termsId);
        if (vo == null) {
            throw new BusinessException(ErrorCode.TERMS_NOT_FOUND);
        }

        return TermsDetailDTO.builder()
                .id(vo.getId())
                .type(vo.getType())
                .title(vo.getTitle())
                .version(vo.getVersion())
                .content(vo.getContent())
                .build();
    }
}
