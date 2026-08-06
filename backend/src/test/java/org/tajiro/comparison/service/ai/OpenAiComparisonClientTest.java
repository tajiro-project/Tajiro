package org.tajiro.comparison.service.ai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.tajiro.common.api.ErrorCode;
import org.tajiro.comparison.dto.ComparisonAnalysisResponseDTO;
import org.tajiro.comparison.dto.ComparisonMetricDTO;
import org.tajiro.exception.BusinessException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OpenAiComparisonClientTest {

    private RestTemplate restTemplate;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        server = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    void generateParsesStructuredCoachingResponse() {
        String responseBody = "{\"choices\":[{\"message\":{\"content\":"
                + "\"{\\\"aiPropertySummaryText\\\":\\\"두 매물을 비교했어요.\\\","
                + "\\\"aiSummary\\\":\\\"112번 매물이 우선순위에 더 적합해요.\\\","
                + "\\\"aiRecommendedPropertyId\\\":112,"
                + "\\\"aiAtp\\\":\\\"계약 전에 관리비 항목을 확인하세요.\\\"}\"}}]}";

        server.expect(requestTo("https://api.openai.com/v1/chat/completions"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer test-key"))
                .andExpect(content().string(containsString(
                        "\"response_format\":{\"type\":\"json_schema\"")))
                .andExpect(content().string(containsString(
                        "\"enum\":[112,119]")))
                .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        ComparisonAnalysisResponseDTO result = clientWith("test-key").generate(
                metrics(),
                Arrays.asList("직주근접", "가성비"));

        assertEquals(112L, result.getAiRecommendedPropertyId());
        assertEquals("두 매물을 비교했어요.", result.getAiPropertySummaryText());
        server.verify();
    }

    @Test
    void generateFailsClearlyWhenApiKeyIsMissing() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> clientWith("").generate(metrics(), Collections.emptyList()));

        assertEquals(ErrorCode.AI_API_KEY_NOT_CONFIGURED, exception.getResponseCode());
    }

    @Test
    void generateRejectsRecommendationOutsideRequestedProperties() {
        String responseBody = "{\"choices\":[{\"message\":{\"content\":"
                + "\"{\\\"aiPropertySummaryText\\\":\\\"요약\\\","
                + "\\\"aiSummary\\\":\\\"추천\\\","
                + "\\\"aiRecommendedPropertyId\\\":999,"
                + "\\\"aiAtp\\\":\\\"확인 사항\\\"}\"}}]}";
        server.expect(requestTo("https://api.openai.com/v1/chat/completions"))
                .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> clientWith("test-key").generate(metrics(), Collections.emptyList()));

        assertEquals(ErrorCode.AI_COACHING_UNAVAILABLE, exception.getResponseCode());
        server.verify();
    }

    private OpenAiComparisonClient clientWith(String apiKey) {
        return new OpenAiComparisonClient(
                restTemplate,
                apiKey,
                "",
                "gpt-4o-mini",
                "");
    }

    private List<ComparisonMetricDTO> metrics() {
        return Arrays.asList(
                ComparisonMetricDTO.builder()
                        .propertyId(112L)
                        .title("277-22 102호")
                        .monthlyRent(20)
                        .maintenanceFee(5)
                        .commuteMinutes(30)
                        .build(),
                ComparisonMetricDTO.builder()
                        .propertyId(119L)
                        .title("282-16 101호")
                        .monthlyRent(38)
                        .maintenanceFee(5)
                        .commuteMinutes(20)
                        .build());
    }
}
