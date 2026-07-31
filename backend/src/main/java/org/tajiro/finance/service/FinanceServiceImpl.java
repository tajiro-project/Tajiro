/**
 * 금융상품 조회 및 조건 매칭 서비스.
 */
package org.tajiro.finance.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.tajiro.finance.dto.FinanceDTO;
import org.tajiro.finance.mapper.FinanceMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FinanceServiceImpl implements FinanceService {

    final private FinanceMapper mapper;

    @Override
    public List<FinanceDTO> getList(String keyword) {
        return mapper
                .getList(keyword)
                .stream()
                .map(FinanceDTO::of)
                .toList();
    }

    @Override
    public FinanceDTO get(Long id) {
        return FinanceDTO.of(mapper.get(id));
    }
}