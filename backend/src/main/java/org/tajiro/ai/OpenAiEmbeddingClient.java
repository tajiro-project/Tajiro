package org.tajiro.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;

import java.io.IOException;
import java.util.*;

import org.springframework.web.client.RestTemplate;
import org.tajiro.common.api.ErrorCode;
import org.tajiro.exception.BusinessException;


@Slf4j
@Component
public class OpenAiEmbeddingClient implements EmbeddingClient {
    private static final String DEFAULT_MODEL = "${ai.embedding-model}";
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String apiKey;
    private final String model;
    private final String apiUrl;


    public OpenAiEmbeddingClient(
            RestTemplate restTemplate,
            @Value("${ai.api-key:}")
            String configuredApiKey,
            @Value("${ai.embedding-model:}")
            String configuredModel,
            @Value("${ai.api-url}")
            String configuredApiUrl) {
        this.restTemplate = restTemplate;
        this.apiKey = configuredApiKey;
        this.model = configuredModel;
        this.apiUrl = configuredApiUrl;
    }

    @Override
    public List<List<Double>> embed(List<String> inputs) {

        if (inputs ==null || inputs.isEmpty()){
            return Collections.emptyList();
        }

        if (!hasText(apiKey)){
            throw new BusinessException (
                    ErrorCode.AI_API_KEY_NOT_CONFIGURED
            );
        }

        if (inputs.stream().anyMatch(input->!hasText(input))){
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        try{
            HttpHeaders headers = new HttpHeaders();

            headers.setContentType(MediaType.APPLICATION_JSON);

            headers.set(HttpHeaders.AUTHORIZATION,"Bearer "+apiKey);

            Map<String,Object>request=new LinkedHashMap<>();

            request.put("model",model);

            request.put("input",inputs);

            request.put("encoding_format","float");

            String body = objectMapper.writeValueAsString(request);

            ResponseEntity<String> response =
                    restTemplate.exchange(
                            apiUrl,
                            HttpMethod.POST,
                            new HttpEntity<>(
                                    body,
                                    headers
                            ),
                            String.class //응답타입
                    );
            return parseEmbeddings(
                    response.getBody(),
                    inputs.size()
            );

        } catch (HttpStatusCodeException e) {

            log.warn(
                    "OpenAI embedding request failed "
                            + "with status {}: {}",
                    e.getStatusCode(),
                    e.getResponseBodyAsString()
            );

            throw unavailable();

        } catch (ResourceAccessException e) {

            log.warn(
                    "OpenAI embedding request "
                            + "timed out or could not connect"
            );

            throw unavailable();

        } catch (RestClientException | IOException e) {

            log.warn(
                    "OpenAI embedding response "
                            + "could not be processed",
                    e
            );

            throw unavailable();
        }
    }

    // OpenAI 응답에서 embedding만 추출
    private List<List<Double>> parseEmbeddings(
            String responseBody,
            int expectedCount
    ) throws IOException {

        if (!hasText(responseBody)) {
            throw unavailable();
        }

        JsonNode data =
                objectMapper
                        .readTree(responseBody)
                        .path("data");

        if (!data.isArray()
                || data.size() != expectedCount) {

            throw unavailable();
        }

        /*
         * OpenAI response index 기준으로
         * 원래 입력 순서를 복원
         */
        List<List<Double>> result =
                new ArrayList<>(
                        Collections.nCopies(
                                expectedCount,
                                null
                        )
                );

        for (JsonNode item : data) {

            int index =
                    item.path("index")
                            .asInt(-1);

            JsonNode embeddingNode =
                    item.path("embedding");

            if (index < 0
                    || index >= expectedCount
                    || !embeddingNode.isArray()) {

                throw unavailable();
            }

            List<Double> vector =
                    new ArrayList<>(
                            embeddingNode.size()
                    );

            for (JsonNode value
                    : embeddingNode) {

                vector.add(
                        value.asDouble()
                );
            }

            result.set(
                    index,
                    vector
            );
        }

        if (result.stream().anyMatch(
                vector ->
                        vector == null
                                || vector.isEmpty()
        )) {

            throw unavailable();
        }

        return result;
    }


    private static boolean hasText(
            String value
    ) {

        return value != null
                && !value.trim().isEmpty();
    }

    private BusinessException unavailable() {

        return new BusinessException(
                ErrorCode.AI_EMBEDDING_UNAVAILABLE
        );
    }
}
