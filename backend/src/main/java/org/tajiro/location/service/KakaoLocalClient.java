package org.tajiro.location.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.tajiro.common.api.ErrorCode;
import org.tajiro.exception.BusinessException;
import org.tajiro.location.dto.AddressResolveResult;

import java.math.BigDecimal;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class KakaoLocalClient {

    private static final String ADDRESS_URL =
            "https://dapi.kakao.com/v2/local/search/address.json";
    private static final String KEYWORD_URL =
            "https://dapi.kakao.com/v2/local/search/keyword.json";
    private static final String COORD2ADDRESS_URL =
            "https://dapi.kakao.com/v2/local/geo/coord2address.json";

    private static final int SEARCH_SIZE = 10;
    private static final int PLACE_SEARCH_SIZE = 15;
    private static final int MAX_RETRY = 3;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String restKey;

    public KakaoLocalClient(
            RestTemplate restTemplate,
            @Value("${kakao.rest-api-key:}") String restKey) {
        this.restTemplate = restTemplate;
        this.restKey = restKey;
    }

    public List<AddressResolveResult> searchAddress(String query) {
        if (!hasText(restKey)) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
        if (!hasText(query)) {
            return Collections.emptyList();
        }

        List<AddressResolveResult> byAddress = searchByAddress(query);
        return byAddress.isEmpty() ? searchByKeyword(query) : byAddress;
    }

    public AddressResolveResult geocode(String address) {
        List<AddressResolveResult> results = searchAddress(address);
        return results.isEmpty() ? null : results.get(0);
    }

    public List<AddressResolveResult> searchPlaces(String query) {
        if (!hasText(restKey)) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
        if (!hasText(query)) {
            return Collections.emptyList();
        }

        JsonNode root = call(UriComponentsBuilder.fromHttpUrl(KEYWORD_URL)
                .queryParam("query", query)
                .queryParam("size", PLACE_SEARCH_SIZE)
                .build()
                .encode(StandardCharsets.UTF_8)
                .toUri());

        List<AddressResolveResult> results = new ArrayList<>();
        for (JsonNode doc : root.path("documents")) {
            BigDecimal lng = decimal(doc.path("x").asText(null));
            BigDecimal lat = decimal(doc.path("y").asText(null));
            if (lat == null || lng == null) {
                continue;
            }
            results.add(AddressResolveResult.base()
                    .roadAddress(text(doc.path("road_address_name")))
                    .jibunAddress(text(doc.path("address_name")))
                    .buildingName(text(doc.path("place_name")))
                    .lat(lat)
                    .lng(lng)
                    .build());
        }
        return results;
    }

    private List<AddressResolveResult> searchByAddress(String query) {
        JsonNode root = call(UriComponentsBuilder.fromHttpUrl(ADDRESS_URL)
                .queryParam("query", query)
                .queryParam("size", SEARCH_SIZE)
                .build()
                .encode(StandardCharsets.UTF_8)
                .toUri());

        List<AddressResolveResult> results = new ArrayList<>();
        for (JsonNode doc : root.path("documents")) {
            AddressResolveResult result = toResult(doc);
            if (result != null) {
                results.add(result);
            }
        }
        return results;
    }

    private List<AddressResolveResult> searchByKeyword(String query) {
        JsonNode root = call(UriComponentsBuilder.fromHttpUrl(KEYWORD_URL)
                .queryParam("query", query)
                .queryParam("size", SEARCH_SIZE)
                .build()
                .encode(StandardCharsets.UTF_8)
                .toUri());

        List<AddressResolveResult> results = new ArrayList<>();
        for (JsonNode doc : root.path("documents")) {
            BigDecimal lng = decimal(doc.path("x").asText(null));
            BigDecimal lat = decimal(doc.path("y").asText(null));
            if (lat == null || lng == null) {
                continue;
            }
            String jibun = text(doc.path("address_name"));
            JsonNode address = addressNodeOf(jibun);
            if(address == null) {
                address = coordToAddress(lat, lng);
            }
            results.add(withParcel(AddressResolveResult.base()
                    .roadAddress(text(doc.path("road_address_name")))
                    .jibunAddress(text(doc.path("address_name")))
                    .buildingName(text(doc.path("place_name")))
                    .lat(lat)
                    .lng(lng), address)
                    .build());
        }
        return results;
    }

    private JsonNode addressNodeOf(String jibunAddress) {
        if (!hasText(jibunAddress)) {
            return null;
        }

        JsonNode root = call(UriComponentsBuilder.fromHttpUrl(ADDRESS_URL)
                .queryParam("query", jibunAddress)
                .queryParam("size", 1)
                .build()
                .encode(StandardCharsets.UTF_8)
                .toUri());

        JsonNode documents = root.path("documents");
        if (!documents.isArray() || documents.size() == 0) {
            return null;
        }

        JsonNode address = documents.get(0).path("address");
        return hasText(text(address.path("b_code"))) ? address : null;
    }

    private AddressResolveResult toResult(JsonNode doc) {
        BigDecimal lng = decimal(doc.path("x").asText(null));
        BigDecimal lat = decimal(doc.path("y").asText(null));
        if (lat == null || lng == null) {
            return null;
        }

        JsonNode address = doc.path("address");
        if (address.isMissingNode() || address.isNull()
                || !hasText(text(address.path("b_code")))) {
            address = coordToAddress(lat, lng);
        }

        JsonNode roadAddress = doc.path("road_address");
        String road = hasText(text(roadAddress.path("address_name")))
                ? text(roadAddress.path("address_name"))
                : text(doc.path("address_name"));

        return withParcel(AddressResolveResult.base()
                .roadAddress(road)
                .jibunAddress(text(address.path("address_name")))
                .buildingName(text(roadAddress.path("building_name")))
                .lat(lat)
                .lng(lng), address)
                .build();
    }

    private AddressResolveResult.AddressResolveResultBuilder withParcel(
            AddressResolveResult.AddressResolveResultBuilder builder, JsonNode address) {

        if (address == null || address.isMissingNode() || address.isNull()) {
            return builder;
        }

        String bCode = text(address.path("b_code"));
        builder.dongName(text(address.path("region_3depth_name")));

        if (!hasText(bCode) || bCode.length() < 10) {
            return builder;
        }

        boolean mountain = "Y".equals(text(address.path("mountain_yn")));
        String platGbCd = mountain ? "1" : "0";
        String bun = pad4(text(address.path("main_address_no")));
        String ji = pad4(text(address.path("sub_address_no")));

        return builder
                .sigunguCd(bCode.substring(0, 5))
                .bjdongCd(bCode.substring(5, 10))
                .platGbCd(platGbCd)
                .bun(bun)
                .ji(ji)
                .pnu(bCode + platGbCd + bun + ji);
    }

    private JsonNode coordToAddress(BigDecimal lat, BigDecimal lng) {
        JsonNode root = call(UriComponentsBuilder.fromHttpUrl(COORD2ADDRESS_URL)
                .queryParam("x", lng)
                .queryParam("y", lat)
                .build()
                .encode(StandardCharsets.UTF_8)
                .toUri());

        JsonNode documents = root.path("documents");
        return documents.isArray() && documents.size() > 0
                ? documents.get(0).path("address")
                : null;
    }

    private JsonNode call(URI uri) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "KakaoAK " + restKey);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        for (int attempt = 0; attempt < MAX_RETRY; attempt++) {
            try {
                ResponseEntity<String> response =
                        restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
                return objectMapper.readTree(response.getBody());
            } catch (HttpClientErrorException.TooManyRequests e) {
                sleep((long) Math.pow(2, attempt) * 500);
            } catch (Exception e) {
                log.warn("[카카오 로컬] 호출 실패: {}", e.getMessage());
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
            }
        }

        log.warn("[카카오 로컬] 쿼터 초과");
        throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    private String text(JsonNode node) {
        String value = node.asText(null);
        return hasText(value) ? value : null;
    }

    private BigDecimal decimal(String value) {
        try {
            return hasText(value) ? new BigDecimal(value) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String pad4(String value) {
        String digits = value == null ? "" : value.replaceAll("\\D", "");
        if (digits.isEmpty()) {
            digits = "0";
        }
        String padded = "0000" + digits;
        return padded.substring(padded.length() - 4);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}