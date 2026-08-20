/**
 * finance 도메인 MyBatis Mapper 인터페이스. XML은 resources/org/tajiro/finance/mapper/ 에 둡니다.
 */
package org.tajiro.finance.mapper;

import org.apache.ibatis.annotations.Param;
import org.tajiro.finance.domain.FinanceCategoryVO;
import org.tajiro.finance.domain.FinanceVO;

import java.math.BigDecimal;
import java.util.List;

public interface FinanceMapper{
    public FinanceVO get(long id);
    public List<FinanceVO> getList(String keyword);
    //매물 trade_type 기반 추천
    List<FinanceVO>
    getRecommendedByPropertyId(
            @Param("propertyId")
            Long propertyId
    );

    // category embedding

    List<FinanceCategoryVO>
    getCategoriesWithoutEmbedding();

    List<FinanceCategoryVO>
    getCategoriesWithEmbedding();

    // product embedding
    List<FinanceVO>
    getProductsWithoutEmbedding();

    List<FinanceVO>
    getProductsWithEmbedding();

    // category embedding 저장

    int updateCategoryEmbedding(

            @Param("id")
            Long id,

            @Param("embedding")
            String embedding
    );

    // financial_product embedding 저장

    int updateProductEmbedding(

            @Param("id")
            Long id,

            @Param("embedding")
            String embedding
    );

    // 최종 9개 분류 결과 저장

    int updateProductClassification(

            @Param("id")
            Long id,

            @Param("tradeType")
            String tradeType,

            @Param("productType")
            String productType,

            @Param("categorySimilarity")
            BigDecimal categorySimilarity
    );
}