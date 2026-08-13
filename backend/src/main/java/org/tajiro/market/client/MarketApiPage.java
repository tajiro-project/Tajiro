package org.tajiro.market.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.tajiro.market.domain.ActualTransactionVO;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketApiPage {
    private List<ActualTransactionVO> items;
    private int pageNo;
    private int numOfRows;
    private int totalCount;
}
