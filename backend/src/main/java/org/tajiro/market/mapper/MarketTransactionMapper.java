package org.tajiro.market.mapper;

import org.apache.ibatis.annotations.Param;
import org.tajiro.market.domain.ActualTransactionVO;
import org.tajiro.market.domain.MarketApiSource;
import org.tajiro.market.domain.MarketComparableTransactionVO;
import org.tajiro.market.domain.MarketEvaluationResult;
import org.tajiro.market.domain.MarketPropertyCategory;
import org.tajiro.market.domain.MarketPropertyVO;
import org.tajiro.market.domain.MarketSyncCoverageVO;
import org.tajiro.market.domain.MarketSyncTarget;
import org.tajiro.market.domain.MarketTradeType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface MarketTransactionMapper {

    int deleteTransactionsForMonth(
            @Param("sourceApi") MarketApiSource sourceApi,
            @Param("sggCode") String sggCode,
            @Param("dealYm") String dealYm);

    int deleteTransactionsBefore(@Param("cutoffDate") LocalDate cutoffDate);

    int deleteCoverageBefore(@Param("cutoffDealYm") String cutoffDealYm);

    int upsertTransactions(
            @Param("transactions") List<ActualTransactionVO> transactions);

    int markSyncSuccess(
            @Param("sourceApi") MarketApiSource sourceApi,
            @Param("sggCode") String sggCode,
            @Param("dealYm") String dealYm,
            @Param("totalCount") int totalCount);

    int markSyncFailure(
            @Param("sourceApi") MarketApiSource sourceApi,
            @Param("sggCode") String sggCode,
            @Param("dealYm") String dealYm,
            @Param("lastError") String lastError);

    boolean hasSuccessfulCoverage(
            @Param("sourceApi") MarketApiSource sourceApi,
            @Param("sggCode") String sggCode,
            @Param("dealYm") String dealYm);

    LocalDateTime findLatestSuccessfulSyncAt();

    List<MarketSyncTarget> findSyncTargets();

    List<MarketPropertyVO> findMarketProperties(
            @Param("propertyIds") List<Long> propertyIds);

    List<MarketPropertyVO> findAllActiveMarketProperties();

    List<MarketComparableTransactionVO> findComparableTransactions(
            @Param("propertyCategory") MarketPropertyCategory propertyCategory,
            @Param("tradeType") MarketTradeType tradeType,
            @Param("sggCode") String sggCode,
            @Param("fromDate") LocalDate fromDate);

    List<MarketSyncCoverageVO> findCoverage(
            @Param("sources") List<MarketApiSource> sources,
            @Param("sggCode") String sggCode,
            @Param("fromDealYm") String fromDealYm);

    int updateEvaluation(MarketEvaluationResult result);
}
