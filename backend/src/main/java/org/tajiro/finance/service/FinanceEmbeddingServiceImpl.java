package org.tajiro.finance.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.tajiro.finance.domain.FinanceCategoryVO;
import org.tajiro.finance.domain.FinanceVO;
import org.tajiro.finance.dto.FinanceEmbeddingResultDTO;
import org.tajiro.finance.mapper.FinanceMapper;
import org.tajiro.ai.EmbeddingClient;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FinanceEmbeddingServiceImpl
        implements FinanceEmbeddingService {

    /*
     * 금융상품은 한 번에 전부 보내지 않고
     * 20개씩 embedding API 호출
     */
    private static final int PRODUCT_EMBEDDING_BATCH_SIZE = 20;

    private static final int EXPECTED_CATEGORY_COUNT = 9; // 월세/전세/매매×담보/보증/기타= 9개

    private final FinanceMapper mapper;

    private final EmbeddingClient
            embeddingClient;

    private final ObjectMapper objectMapper =
            new ObjectMapper();

    /**
     * 전체 초기화 흐름
     *
     * 1. 카테고리 embedding 생성
     * 2. 금융상품 embedding 생성
     * 3. cosine similarity 계산
     * 4. 최종 카테고리 저장
     */
    @Override
    public FinanceEmbeddingResultDTO
    initializeAndClassify() {

        int categoryEmbeddingCount =
                embedMissingCategories();

        int productEmbeddingCount =
                embedMissingProducts();

        int classifiedProductCount =
                classifyProducts();

        return FinanceEmbeddingResultDTO
                .builder()

                .categoryEmbeddingCount(
                        categoryEmbeddingCount
                )

                .productEmbeddingCount(
                        productEmbeddingCount
                )

                .classifiedProductCount(
                        classifiedProductCount
                )

                .build();
    }

    /**
     * 아직 embedding이 없는
     * 카테고리만 처리
     */
    private int embedMissingCategories() {

        List<FinanceCategoryVO> categories =
                mapper.getCategoriesWithoutEmbedding();

        if (categories.isEmpty()) {
            return 0;
        }

        /*
         * financial_product_category.description
         *
         * 9개 문장을 embedding
         */
        List<String> descriptions =
                categories.stream()
                        .map(
                                FinanceCategoryVO
                                        ::getDescription
                        )
                        .toList();

        List<List<Double>> vectors =
                embeddingClient.embed(
                        descriptions
                );

        for (int i = 0;
             i < categories.size();
             i++) {

            mapper.updateCategoryEmbedding(
                    categories.get(i).getId(),
                    toJson(vectors.get(i))
            );
        }

        return categories.size();
    }

    /**
     * embedding이 없는 금융상품만 처리
     */
    private int embedMissingProducts() {

        List<FinanceVO> products =
                mapper.getProductsWithoutEmbedding();

        int updatedCount = 0;

        /*
         * 20개씩 batch 처리
         */
        for (
                int start = 0;
                start < products.size();
                start += PRODUCT_EMBEDDING_BATCH_SIZE
        ) {

            int end =
                    Math.min(
                            start
                                    + PRODUCT_EMBEDDING_BATCH_SIZE,
                            products.size()
                    );
            List<FinanceVO> batch =
                    products.subList(
                            start,
                            end
                    );

            /*
             * 금융상품당
             *
             * 상품명 + eligibility
             *
             * 하나의 문자열 생성
             */
            List<String> inputs =
                    batch.stream()
                            .map(
                                    this
                                            ::buildProductEmbeddingText
                            )
                            .toList();

            List<List<Double>> vectors =
                    embeddingClient.embed(
                            inputs
                    );

            for (
                    int i = 0;
                    i < batch.size();
                    i++
            ) {

                mapper.updateProductEmbedding(
                        batch.get(i).getId(),
                        toJson(vectors.get(i))
                );

                updatedCount++;
            }
        }

        return updatedCount;
    }

    /**
     * 금융상품 embedding
     *
     * VS
     *
     * 9개 category embedding
     *
     * cosine similarity 비교
     */
    private int classifyProducts() {

        List<FinanceCategoryVO> categories =
                mapper.getCategoriesWithEmbedding();

        /*
         * 9개가 전부 embedding 되어 있어야 함
         */
        if (categories.size()
                != EXPECTED_CATEGORY_COUNT) {

            throw new IllegalStateException(
                    "금융상품 카테고리 9개의 "
                            + "embedding이 모두 필요합니다. "
                            + "현재: "
                            + categories.size()
            );
        }

        /*
         * DB JSON 문자열
         * →
         * List<Double>
         */
        List<CategoryVector> categoryVectors =
                categories.stream()

                        .map(
                                category ->
                                        new CategoryVector(
                                                category,
                                                fromJson(category.getEmbedding())
                                        )
                        )

                        .toList();

        /*
         * embedding이 존재하는
         * 금융상품 전체 조회
         */
        List<FinanceVO> products =
                mapper.getProductsWithEmbedding();

        int classifiedCount = 0;

        for (FinanceVO product : products) {

            List<Double> productVector =
                    fromJson(
                            product.getEmbedding()
                    );

            FinanceCategoryVO bestCategory =
                    null;

            double bestScore =
                    -Double.MAX_VALUE;

            /*
             * 9개 category와 비교
             */
            for (
                    CategoryVector categoryVector
                    : categoryVectors
            ) {

                double score =
                        cosineSimilarity(
                                productVector,
                                categoryVector.vector
                        );

                /*
                 * 현재까지 가장 높은 점수
                 */
                if (score > bestScore) {

                    bestScore =
                            score;

                    bestCategory =
                            categoryVector.category;
                }
            }

            if (bestCategory == null) {
                continue;
            }

            /*
             * DB category_similarity
             * DECIMAL(7,6)
             */
            BigDecimal similarity =
                    BigDecimal
                            .valueOf(bestScore)
                            .setScale(
                                    6,
                                    RoundingMode.HALF_UP
                            );

            /*
             * 가장 유사했던 category의
             *
             * trade_type
             * product_type
             * similarity
             *
             * 를 financial_product에 저장
             */
            mapper.updateProductClassification(

                    product.getId(),

                    bestCategory
                            .getTradeType(),

                    bestCategory
                            .getProductType(),

                    similarity
            );

            classifiedCount++;
        }

        return classifiedCount;
    }

    /**
     * 금융상품 embedding에 사용하는 실제 문자열
     */
    private String buildProductEmbeddingText(
            FinanceVO product
    ) {

        String eligibility =
                product.getEligibility();

        if (eligibility == null
                || eligibility.isBlank()) {

            eligibility =
                    "별도 가입 조건 정보 없음";
        }

        return "상품명: "
                + product.getProductName()

                + "\n가입조건: "

                + eligibility;
    }

    /**
     * cosine similarity
     *
     * A · B
     * -------------------
     * ||A|| × ||B||
     */
    private double cosineSimilarity(
            List<Double> a,
            List<Double> b
    ) {

        if (
                a == null
                        || b == null
                        || a.isEmpty()
                        || a.size() != b.size()
        ) {

            throw new IllegalArgumentException(
                    "embedding 벡터의 차원이 일치하지 않습니다."
            );
        }

        double dot = 0.0;

        double normA = 0.0;
        double normB = 0.0;

        for (
                int i = 0;
                i < a.size();
                i++
        ) {

            double av =
                    a.get(i);

            double bv =
                    b.get(i);

            dot +=
                    av * bv;

            normA +=
                    av * av;

            normB +=
                    bv * bv;
        }

        if (
                normA == 0.0
                        || normB == 0.0
        ) {

            return 0.0;
        }

        return dot
                /
                (
                        Math.sqrt(normA)
                                *
                                Math.sqrt(normB)
                );
    }

    /**
     * Java List<Double>
     * →
     * MySQL JSON 문자열
     */
    private String toJson(
            List<Double> vector
    ) {

        try {

            return objectMapper
                    .writeValueAsString(
                            vector
                    );

        } catch (IOException e) {

            throw new IllegalStateException(
                    "embedding JSON 직렬화에 실패했습니다.",
                    e
            );
        }
    }

    /**
     * MySQL JSON 문자열
     * →
     * Java List<Double>
     */
    private List<Double> fromJson(
            String json
    ) {

        try {

            return objectMapper.readValue(

                    json,

                    new TypeReference<
                            List<Double>
                            >() {
                    }
            );

        } catch (IOException e) {

            throw new IllegalStateException(
                    "embedding JSON 역직렬화에 실패했습니다.",
                    e
            );
        }
    }

    /**
     * 카테고리 VO와
     * 파싱된 embedding vector를
     * 함께 보관
     */
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static class CategoryVector {
        private final FinanceCategoryVO category;
        private final List<Double> vector;
    }
}