package org.tajiro.terms.service;

import org.tajiro.terms.dto.TermsDTO;
import org.tajiro.terms.dto.TermsDetailDTO;

import java.util.List;

public interface TermsService {

    List<TermsDTO> getEffectiveTerms();

    TermsDetailDTO getTermsDetail(Long termsId);
}
