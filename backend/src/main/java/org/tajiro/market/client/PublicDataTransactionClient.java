package org.tajiro.market.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.tajiro.market.domain.ActualTransactionVO;
import org.tajiro.market.domain.MarketApiSource;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class PublicDataTransactionClient {

    private static final DateTimeFormatter DEAL_MONTH_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMM");

    private final RestTemplate restTemplate;
    private final PublicDataXmlParser parser;
    private final String serviceKey;
    private final boolean encodedServiceKey;
    private final int numOfRows;

    public PublicDataTransactionClient(
            @Qualifier("openAiRestTemplate") RestTemplate restTemplate,
            PublicDataXmlParser parser,
            @Value("${market.api.service-key:}") String configuredServiceKey,
            @Value("${PUBLIC_DATA_API_KEY:}") String environmentServiceKey,
            @Value("${market.api.service-key-encoded:false}") boolean encodedServiceKey,
            @Value("${market.api.num-of-rows:1000}") int numOfRows) {
        this.restTemplate = restTemplate;
        this.parser = parser;
        this.serviceKey = firstNonBlank(environmentServiceKey, configuredServiceKey);
        this.encodedServiceKey = encodedServiceKey;
        this.numOfRows = Math.max(1, numOfRows);
    }

    public boolean isConfigured() {
        return serviceKey != null && !serviceKey.trim().isEmpty();
    }

    public List<ActualTransactionVO> fetch(
            MarketApiSource source,
            String sggCode,
            YearMonth dealMonth) {
        if (!isConfigured()) {
            throw new MarketApiException("공공데이터포털 실거래가 API 키가 설정되지 않았습니다.");
        }
        if (sggCode == null || !sggCode.matches("\\d{5}")) {
            throw new IllegalArgumentException("시군구 코드는 5자리 숫자여야 합니다.");
        }

        String dealYm = dealMonth.format(DEAL_MONTH_FORMAT);
        MarketApiPage firstPage = fetchPage(source, sggCode, dealYm, 1);
        List<ActualTransactionVO> result = new ArrayList<>(firstPage.getItems());

        int responsePageSize = firstPage.getNumOfRows() > 0
                ? firstPage.getNumOfRows()
                : numOfRows;
        int totalPages = Math.max(1,
                (int) Math.ceil((double) firstPage.getTotalCount() / responsePageSize));
        for (int pageNo = 2; pageNo <= totalPages; pageNo++) {
            result.addAll(fetchPage(source, sggCode, dealYm, pageNo).getItems());
        }
        if (result.size() != firstPage.getTotalCount()) {
            throw new MarketApiException(
                    source + " API 페이지 수집 건수가 totalCount와 다릅니다. sggCode="
                            + sggCode + ", dealYm=" + dealYm
                            + ", expected=" + firstPage.getTotalCount()
                            + ", actual=" + result.size());
        }

        prepareSnapshotRows(result, dealYm);
        log.info("실거래가 수집 완료 source={}, sggCode={}, dealYm={}, count={}",
                source, sggCode, dealYm, result.size());
        return result;
    }

    private MarketApiPage fetchPage(
            MarketApiSource source,
            String sggCode,
            String dealYm,
            int pageNo) {
        URI uri = buildUri(source, sggCode, dealYm, pageNo);
        try {
            String response = restTemplate.getForObject(uri, String.class);
            return parser.parse(source, sggCode, response);
        } catch (MarketApiException e) {
            throw e;
        } catch (RestClientException e) {
            throw new MarketApiException(
                    source + " API 호출에 실패했습니다. sggCode=" + sggCode
                            + ", dealYm=" + dealYm + ", pageNo=" + pageNo,
                    e);
        }
    }

    void prepareSnapshotRows(List<ActualTransactionVO> transactions, String dealYm) {
        Map<String, Integer> occurrences = new HashMap<>();
        for (ActualTransactionVO transaction : transactions) {
            transaction.setSourceDealYm(dealYm);
            String baseFingerprint = transaction.getSourceUniqueKey();
            int occurrence = occurrences.merge(baseFingerprint, 1, Integer::sum);
            transaction.setSourceUniqueKey(
                    TransactionFingerprint.withOccurrence(baseFingerprint, occurrence));
        }
    }

    URI buildUri(
            MarketApiSource source,
            String sggCode,
            String dealYm,
            int pageNo) {
        String queryServiceKey = encodedServiceKey
                ? serviceKey
                : encodeQueryValue(serviceKey);
        String url = source.getEndpoint()
                + "?serviceKey=" + queryServiceKey
                + "&LAWD_CD=" + sggCode
                + "&DEAL_YMD=" + dealYm
                + "&pageNo=" + pageNo
                + "&numOfRows=" + numOfRows;
        return URI.create(url);
    }

    private String encodeQueryValue(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8)
                .replace("+", "%20");
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return "";
    }
}
