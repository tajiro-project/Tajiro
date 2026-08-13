package org.tajiro.policy.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import org.tajiro.policy.domain.PolicyCategoryVO;
import org.tajiro.policy.domain.PolicyVO;

import org.tajiro.policy.dto.PolicyEmbeddingResultDTO;

import org.tajiro.policy.mapper.PolicyMapper;

import org.tajiro.ai.EmbeddingClient;

import java.math.BigDecimal;
import java.math.RoundingMode;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PolicyEmbeddingServiceImpl
        implements PolicyEmbeddingService {

    /*
     * OpenAI에 한 번에 보내는 개수
     */
    private static final int
            EMBEDDING_BATCH_SIZE = 20;


    private static final int
            CATEGORY_COUNT = 9;


    private final PolicyMapper mapper;

    private final EmbeddingClient embeddingClient;


    private final ObjectMapper objectMapper =
            new ObjectMapper();


    @Override
    public PolicyEmbeddingResultDTO
    initializeAndClassify(
            int limit
    ) {

        if (
                limit < 1
                        || limit > 500
        ) {

            throw new IllegalArgumentException(
                    "limit은 1~500 사이여야 합니다."
            );
        }


        /*
         * 1.
         * 9개 카테고리 embedding 생성
         */
        int categoryEmbeddingCount =
                embedMissingCategories();


        /*
         * 2.
         * 정책 embedding 생성
         */
        int policyEmbeddingCount =
                embedMissingPolicies(
                        limit
                );


        /*
         * 3.
         * 정책을 9개 중 하나로 분류
         */
        int classifiedPolicyCount =
                classifyPolicies(
                        limit
                );


        return PolicyEmbeddingResultDTO
                .builder()

                .categoryEmbeddingCount(
                        categoryEmbeddingCount
                )

                .policyEmbeddingCount(
                        policyEmbeddingCount
                )

                .classifiedPolicyCount(
                        classifiedPolicyCount
                )

                .remainingEmbeddingCount(
                        mapper
                                .countPoliciesWithoutEmbedding()
                )

                .remainingClassificationCount(
                        mapper
                                .countPoliciesWithoutClassification()
                )

                .build();
    }


    /*
     * =========================
     * Category embedding
     * =========================
     */
    private int embedMissingCategories() {

        List<PolicyCategoryVO> categories =
                mapper
                        .getCategoriesWithoutEmbedding();


        if (categories.isEmpty()) {

            return 0;
        }


        List<String> descriptions =
                categories
                        .stream()

                        .map(
                                PolicyCategoryVO
                                        ::getDescription
                        )

                        .toList();


        List<List<Double>> vectors =
                embeddingClient.embed(
                        descriptions
                );


        for (
                int i = 0;
                i < categories.size();
                i++
        ) {

            mapper
                    .updateCategoryEmbedding(

                            categories
                                    .get(i)
                                    .getId(),

                            toJson(
                                    vectors.get(i)
                            )
                    );
        }


        return categories.size();
    }


    /*
     * =========================
     * Policy embedding
     * =========================
     */
    private int embedMissingPolicies(
            int limit
    ) {

        List<PolicyVO> policies =
                mapper
                        .getPoliciesWithoutEmbedding(
                                limit
                        );


        int count = 0;


        for (
                int start = 0;
                start < policies.size();
                start += EMBEDDING_BATCH_SIZE
        ) {

            int end =
                    Math.min(

                            start
                                    + EMBEDDING_BATCH_SIZE,

                            policies.size()
                    );


            List<PolicyVO> batch =
                    policies.subList(
                            start,
                            end
                    );


            List<String> inputs =
                    batch
                            .stream()

                            .map(
                                    this
                                            ::buildEmbeddingText
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

                mapper
                        .updatePolicyEmbedding(

                                batch
                                        .get(i)
                                        .getId(),

                                toJson(
                                        vectors.get(i)
                                )
                        );


                count++;
            }
        }


        return count;
    }


    /*
     * =========================
     * Classification
     * =========================
     */
    private int classifyPolicies(
            int limit
    ) {

        List<PolicyCategoryVO> categories =
                mapper
                        .getCategoriesWithEmbedding();


        if (
                categories.size()
                        != CATEGORY_COUNT
        ) {

            throw new IllegalStateException(
                    "정책 카테고리 9개의 embedding이 필요합니다."
            );
        }


        List<CategoryVector> categoryVectors =
                categories
                        .stream()

                        .map(
                                category ->

                                        new CategoryVector(

                                                category,

                                                fromJson(
                                                        category
                                                                .getEmbedding()
                                                )
                                        )
                        )

                        .toList();


        List<PolicyVO> policies =
                mapper
                        .getPoliciesToClassify(
                                limit
                        );


        int count = 0;


        for (PolicyVO policy : policies) {

            List<Double> policyVector =
                    fromJson(
                            policy.getEmbedding()
                    );


            PolicyCategoryVO bestCategory =
                    null;


            double bestScore =
                    -Double.MAX_VALUE;


            /*
             * 한 정책을
             * 9개 category와 모두 비교
             */
            for (
                    CategoryVector categoryVector
                    : categoryVectors
            ) {

                double score =
                        cosineSimilarity(

                                policyVector,

                                categoryVector
                                        .vector
                        );


                if (score > bestScore) {

                    bestScore =
                            score;


                    bestCategory =
                            categoryVector
                                    .category;
                }
            }


            if (bestCategory == null) {

                continue;
            }


            BigDecimal similarity =
                    BigDecimal
                            .valueOf(
                                    bestScore
                            )

                            .setScale(
                                    6,
                                    RoundingMode.HALF_UP
                            );


            mapper
                    .updatePolicyClassification(

                            policy.getId(),

                            bestCategory.getId(),

                            similarity
                    );


            count++;
        }


        return count;
    }


    /*
     * 정책을 embedding할 실제 문장
     */
    private String buildEmbeddingText(
            PolicyVO policy
    ) {

        return "정책명: "
                + safe(
                policy.getTitle()
        )

                + "\n지원내용: "

                + safe(
                policy.getDescription()
        );
    }


    private String safe(
            String value
    ) {

        return value == null
                ? ""
                : value.trim();
    }


    /*
     * cosine similarity
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
                    "embedding vector 차원이 일치하지 않습니다."
            );
        }


        double dot =
                0.0;

        double normA =
                0.0;

        double normB =
                0.0;


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


    private String toJson(
            List<Double> vector
    ) {

        try {

            return objectMapper
                    .writeValueAsString(
                            vector
                    );

        } catch (
                JsonProcessingException e
        ) {

            throw new IllegalStateException(
                    "embedding JSON 직렬화 실패",
                    e
            );
        }
    }


    private List<Double> fromJson(
            String json
    ) {

        try {

            return objectMapper
                    .readValue(

                            json,

                            new TypeReference<
                                    List<Double>
                                    >() {
                            }
                    );

        } catch (
                JsonProcessingException e
        ) {

            throw new IllegalStateException(
                    "embedding JSON 역직렬화 실패",
                    e
            );
        }
    }


    private static class CategoryVector {

        private final PolicyCategoryVO
                category;

        private final List<Double>
                vector;


        private CategoryVector(

                PolicyCategoryVO category,

                List<Double> vector
        ) {

            this.category =
                    category;

            this.vector =
                    vector;
        }
    }
}