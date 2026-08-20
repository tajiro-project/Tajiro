package org.tajiro.property.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class KakaoSearchClient {

    private static final String CATEGORY_URL = "https://dapi.kakao.com/v2/local/search/category.json";
    private static final String KEYWORD_URL = "https://dapi.kakao.com/v2/local/search/keyword.json";
    private static final int MAX_RETRY = 3;

    @Value("${kakao.rest-key:${kakao.rest.key:${kakao.rest-api-key:}}}")
    private String kakaoRestKey;

    private final ObjectMapper mapper = new ObjectMapper();

    public KakaoSearchClient() {
    }

    public JsonNode searchCategory(String code, double lat, double lng, int radius) throws Exception {
        String urlStr = CATEGORY_URL + "?category_group_code=" + code
                + "&x=" + lng + "&y=" + lat + "&radius=" + radius
                + "&size=15&page=1&sort=distance";
        return call(urlStr);
    }

    public JsonNode searchKeyword(String query, double lat, double lng, int radius, int page) throws Exception {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8).replace("+", "%20");
        String urlStr = KEYWORD_URL + "?query=" + encodedQuery
                + "&x=" + lng + "&y=" + lat + "&radius=" + radius
                + "&size=15&page=" + page + "&sort=distance";
        return call(urlStr);
    }

    private JsonNode call(String urlStr) throws Exception {
        if (kakaoRestKey == null || kakaoRestKey.trim().isEmpty()) {
            throw new IllegalStateException("Kakao REST API 키가 설정되지 않았습니다. application.properties를 확인해 주세요.");
        }

        for (int i = 0; i < MAX_RETRY; i++) {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "KakaoAK " + kakaoRestKey.trim());
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(5000);

            int status = conn.getResponseCode();
            if (status == 429) { // Rate limit 초과 시 exponential backoff
                long backoff = (long) Math.pow(2, i) * 500;
                Thread.sleep(backoff);
                continue;
            }

            if (status != 200) {
                try (InputStream err = conn.getErrorStream()) {
                    String errorMsg = err != null ? new String(err.readAllBytes(), StandardCharsets.UTF_8) : "";
                    throw new RuntimeException("Kakao API HTTP Error " + status + ": " + errorMsg);
                }
            }

            try (InputStream in = conn.getInputStream()) {
                return mapper.readTree(in); // Sleep 제거로 응답 속도 향상
            }
        }
        throw new RuntimeException("Kakao API Rate Limit Exceeded");
    }
}