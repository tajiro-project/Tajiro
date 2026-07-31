/**
 * finance 도메인 MyBatis Mapper 인터페이스. XML은 resources/org/tajiro/finance/mapper/ 에 둡니다.
 */
package org.tajiro.finance.mapper;

import org.tajiro.finance.domain.FinanceVO;

import java.util.List;

public interface FinanceMapper{
    public FinanceVO get(long id);
    public List<FinanceVO> getList(String keyword);
}