package org.tajiro.finance.service;

import org.tajiro.finance.dto.FinanceDTO;

import java.util.List;

public interface FinanceService {
    public List<FinanceDTO> getList(String keyword);

    public FinanceDTO get(Long id);

    List<FinanceDTO>
    getRecommendedByPropertyId(
            Long propertyId
    );
}