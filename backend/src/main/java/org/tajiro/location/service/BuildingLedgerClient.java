package org.tajiro.location.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.tajiro.location.dto.AddressResolveResult;
import org.tajiro.location.dto.BuildingLedgerItem;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Component
public class BuildingLedgerClient {

    private static final String TITLE_URL =
            "https://apis.data.go.kr/1613000/BldRgstHubService/getBrTitleInfo";

    private static final Pattern PK_PATTERN =
            Pattern.compile("(\"mgmBldrgstPk\"\\s*:\\s*)(\\d+)");

    private static final Pattern NON_DIGIT = Pattern.compile("\\D");

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String serviceKey;

    public BuildingLedgerClient(
            RestTemplate restTemplate,
            @Value("${building-ledger.service-key:}") String serviceKey) {
        this.restTemplate = restTemplate;
        this.serviceKey = serviceKey;
    }

    public List<BuildingLedgerItem> fetchTitleItems(AddressResolveResult address) {
        if (!hasText(serviceKey)) {
            log.warn("[건축물대장] 서비스키가 설정되지 않았습니다");
            return Collections.emptyList();
        }
        if (address == null || !hasText(address.getSigunguCd())) {
            return Collections.emptyList();
        }

        try {
            String body = restTemplate.getForObject(buildUri(address), String.class);
            if (body == null) {
                return Collections.emptyList();
            }

            String trimmed = body.trim();
            if (!trimmed.startsWith("{")) {
                log.warn("[건축물대장] JSON 이 아닌 응답: {}",
                        trimmed.substring(0, Math.min(200, trimmed.length())));
                return Collections.emptyList();
            }

            JsonNode root = objectMapper.readTree(PK_PATTERN.matcher(trimmed).replaceAll("$1\"$2\""));
            JsonNode response = root.path("response");

            String resultCode = response.path("header").path("resultCode").asText("");
            if (!"00".equals(resultCode)) {
                log.warn("[건축물대장] 조회 실패 resultCode={} msg={}",
                        resultCode, response.path("header").path("resultMsg").asText(""));
                return Collections.emptyList();
            }

            return toItems(response.path("body").path("items").path("item"));

        } catch (Exception e) {
            log.warn("[건축물대장] 조회 실패 pnu={}", address.getPnu(), e);
            return Collections.emptyList();
        }
    }

    public List<String> extractDongs(List<BuildingLedgerItem> items) {
        LinkedHashSet<String> dongs = new LinkedHashSet<>();
        for (BuildingLedgerItem item : items) {
            String normalized = normalizeDong(item.getDongNm());
            if (normalized != null) {
                dongs.add(normalized);
            }
        }
        List<String> result = new ArrayList<>(dongs);
        result.sort(BuildingLedgerClient::compareDong);
        return result;
    }

    private String normalizeDong(String dongNm) {
        if (!hasText(dongNm)) {
            return null;
        }
        String trimmed = dongNm.trim();
        return trimmed.matches("\\d+") ? trimmed + "동" : trimmed;
    }

    public BuildingLedgerItem pick(
            List<BuildingLedgerItem> items, String dongNm, String buildingName) {

        if (items.isEmpty()) {
            return null;
        }

        String dongKey = digitsOf(dongNm);
        if (hasText(dongKey)) {
            for (BuildingLedgerItem item : items) {
                if (dongKey.equals(digitsOf(item.getDongNm()))) {
                    return item;
                }
            }
        }

        if (hasText(buildingName)) {
            for (BuildingLedgerItem item : items) {
                if (hasText(item.getBldNm()) && item.getBldNm().contains(buildingName)) {
                    return item;
                }
            }
        }

        return items.get(0);
    }

    private URI buildUri(AddressResolveResult address) {
        String url = TITLE_URL
                + "?serviceKey=" + encode(serviceKey)
                + "&sigunguCd=" + address.getSigunguCd()
                + "&bjdongCd=" + address.getBjdongCd()
                + "&platGbCd=" + orDefault(address.getPlatGbCd(), "0")
                + "&bun=" + orDefault(address.getBun(), "0000")
                + "&ji=" + orDefault(address.getJi(), "0000")
                + "&numOfRows=100&pageNo=1&_type=json";
        return URI.create(url);
    }

    private List<BuildingLedgerItem> toItems(JsonNode item) {
        List<BuildingLedgerItem> items = new ArrayList<>();

        if (item.isArray()) {
            for (JsonNode node : item) {
                items.add(toItem(node));
            }
        } else if (item.isObject()) {
            items.add(toItem(item));
        }
        return items;
    }

    private BuildingLedgerItem toItem(JsonNode node) {
        return BuildingLedgerItem.builder()
                .mgmBldrgstPk(node.path("mgmBldrgstPk").asText(null))
                .dongNm(node.path("dongNm").asText(null))
                .bldNm(node.path("bldNm").asText(null))
                .build();
    }

    private static int compareDong(String a, String b) {
        String da = digitsOf(a);
        String db = digitsOf(b);
        if (hasStatic(da) && hasStatic(db)) {
            return Long.compare(Long.parseLong(da), Long.parseLong(db));
        }
        return a.compareTo(b);
    }

    private static String digitsOf(String value) {
        return value == null ? "" : NON_DIGIT.matcher(value).replaceAll("");
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String orDefault(String value, String fallback) {
        return hasText(value) ? value : fallback;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static boolean hasStatic(String value) {
        return value != null && !value.isEmpty();
    }
}