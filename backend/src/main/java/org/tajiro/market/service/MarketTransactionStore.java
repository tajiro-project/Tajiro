package org.tajiro.market.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tajiro.market.domain.ActualTransactionVO;
import org.tajiro.market.domain.MarketApiSource;
import org.tajiro.market.mapper.MarketTransactionMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MarketTransactionStore {

    private static final int BATCH_SIZE = 500;

    private final MarketTransactionMapper mapper;
    private final MarketPriceConverter priceConverter;

    @Transactional
    public void storeSuccess(
            MarketApiSource source,
            String sggCode,
            String dealYm,
            List<ActualTransactionVO> transactions) {
        for (ActualTransactionVO transaction : transactions) {
            transaction.setSourceDealYm(dealYm);
            transaction.setConvertedPrice(priceConverter.convert(transaction));
        }

        mapper.deleteTransactionsForMonth(source, sggCode, dealYm);

        for (int start = 0; start < transactions.size(); start += BATCH_SIZE) {
            int end = Math.min(start + BATCH_SIZE, transactions.size());
            mapper.upsertTransactions(transactions.subList(start, end));
        }
        mapper.markSyncSuccess(source, sggCode, dealYm, transactions.size());
    }

    @Transactional
    public void storeFailure(
            MarketApiSource source,
            String sggCode,
            String dealYm,
            String errorMessage) {
        String safeMessage = errorMessage == null ? "알 수 없는 오류" : errorMessage;
        mapper.markSyncFailure(source, sggCode, dealYm, safeMessage);
    }
}
