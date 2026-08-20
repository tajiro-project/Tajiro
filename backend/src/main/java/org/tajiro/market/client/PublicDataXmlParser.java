package org.tajiro.market.client;

import org.springframework.stereotype.Component;
import org.tajiro.market.domain.ActualTransactionVO;
import org.tajiro.market.domain.MarketApiSource;
import org.tajiro.market.domain.MarketTradeType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class PublicDataXmlParser {

    public MarketApiPage parse(
            MarketApiSource source,
            String requestedSggCode,
            String xml) {
        if (xml == null || xml.trim().isEmpty()) {
            throw new MarketApiException(source + " API가 빈 응답을 반환했습니다.");
        }

        try {
            Document document = parseSecurely(xml);
            validateApiResult(source, document);

            NodeList nodes = document.getElementsByTagName("item");
            List<ActualTransactionVO> transactions = new ArrayList<>(nodes.getLength());
            for (int index = 0; index < nodes.getLength(); index++) {
                Node node = nodes.item(index);
                if (node instanceof Element) {
                    transactions.add(parseItem(source, requestedSggCode, (Element) node));
                }
            }

            int pageNo = integer(text(document, "pageNo"), 1);
            int numOfRows = integer(text(document, "numOfRows"), Math.max(nodes.getLength(), 1));
            int totalCount = integer(text(document, "totalCount"), nodes.getLength());

            return MarketApiPage.builder()
                    .items(transactions)
                    .pageNo(pageNo)
                    .numOfRows(numOfRows)
                    .totalCount(totalCount)
                    .build();
        } catch (MarketApiException e) {
            throw e;
        } catch (Exception e) {
            throw new MarketApiException(source + " XML 응답을 해석하지 못했습니다.", e);
        }
    }

    private ActualTransactionVO parseItem(
            MarketApiSource source,
            String requestedSggCode,
            Element item) {
        Integer monthlyRent = money(text(item, "monthlyRent"));
        MarketTradeType tradeType = source.isRental()
                ? (monthlyRent != null && monthlyRent > 0
                    ? MarketTradeType.MONTHLY_RENT
                    : MarketTradeType.JEONSE)
                : MarketTradeType.SALE;

        ActualTransactionVO transaction = ActualTransactionVO.builder()
                .sourceApi(source)
                .propertyCategory(source.getPropertyCategory())
                .tradeType(tradeType)
                .sggCode(firstNonBlank(text(item, "sggCd"), requestedSggCode))
                .legalDongCode(text(item, "umdCd"))
                .umdName(text(item, "umdNm"))
                .jibun(text(item, "jibun"))
                .buildingName(firstNonBlank(
                        text(item, "aptNm"),
                        text(item, "offiNm"),
                        text(item, "mhouseNm")))
                .sourceBuildingCode(text(item, "aptSeq"))
                .buildingDong(text(item, "aptDong"))
                .houseType(text(item, "houseType"))
                .exclusiveAreaM2(decimal(text(item, "excluUseAr")))
                .totalFloorAreaM2(decimal(text(item, "totalFloorAr")))
                .landAreaM2(decimal(firstNonBlank(
                        text(item, "landAr"),
                        text(item, "plottageAr"))))
                .floor(integer(text(item, "floor"), null))
                .buildYear(integer(text(item, "buildYear"), null))
                .dealDate(dealDate(item))
                .dealAmount(money(text(item, "dealAmount")))
                .deposit(money(text(item, "deposit")))
                .monthlyRent(monthlyRent)
                .canceled(hasText(text(item, "cdealType"))
                        || hasText(text(item, "cdealDay")))
                .cancellationDate(text(item, "cdealDay"))
                .build();

        validateRequiredValues(transaction);
        transaction.setSourceUniqueKey(TransactionFingerprint.create(transaction));
        return transaction;
    }

    private void validateRequiredValues(ActualTransactionVO transaction) {
        if (!hasText(transaction.getSggCode()) || transaction.getDealDate() == null) {
            throw new MarketApiException(
                    transaction.getSourceApi() + " 응답에 지역코드 또는 계약일이 없습니다.");
        }
        if (transaction.getTradeType() == MarketTradeType.SALE
                && transaction.getDealAmount() == null) {
            throw new MarketApiException(
                    transaction.getSourceApi() + " 매매 응답에 거래금액이 없습니다.");
        }
        if (transaction.getTradeType() != MarketTradeType.SALE
                && transaction.getDeposit() == null) {
            throw new MarketApiException(
                    transaction.getSourceApi() + " 임대차 응답에 보증금이 없습니다.");
        }
    }

    private LocalDate dealDate(Element item) {
        Integer year = integer(text(item, "dealYear"), null);
        Integer month = integer(text(item, "dealMonth"), null);
        Integer day = integer(text(item, "dealDay"), null);
        if (year == null || month == null || day == null) {
            return null;
        }
        try {
            return LocalDate.of(year, month, day);
        } catch (DateTimeException e) {
            throw new MarketApiException("올바르지 않은 실거래 계약일입니다.", e);
        }
    }

    private Document parseSecurely(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new InputSource(new StringReader(xml)));
    }

    private void validateApiResult(MarketApiSource source, Document document) {
        String resultCode = text(document, "resultCode");
        if ("000".equals(resultCode) || "00".equals(resultCode)) {
            return;
        }

        String resultMessage = firstNonBlank(
                text(document, "resultMsg"),
                text(document, "returnAuthMsg"),
                text(document, "errMsg"),
                "알 수 없는 오류");
        throw new MarketApiException(
                source + " API 오류: " + resultCode + " " + resultMessage);
    }

    private String text(Document document, String tagName) {
        NodeList nodes = document.getElementsByTagName(tagName);
        return nodes.getLength() == 0 ? null : clean(nodes.item(0).getTextContent());
    }

    private String text(Element element, String tagName) {
        NodeList nodes = element.getElementsByTagName(tagName);
        return nodes.getLength() == 0 ? null : clean(nodes.item(0).getTextContent());
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }
        String cleaned = value.trim();
        return cleaned.isEmpty() ? null : cleaned;
    }

    private BigDecimal decimal(String value) {
        if (!hasText(value)) {
            return null;
        }
        try {
            return new BigDecimal(value.replace(",", "").trim());
        } catch (NumberFormatException e) {
            throw new MarketApiException("숫자 필드를 해석하지 못했습니다: " + value, e);
        }
    }

    private Integer money(String value) {
        BigDecimal amount = decimal(value);
        return amount == null ? null : amount.intValueExact();
    }

    private Integer integer(String value, Integer defaultValue) {
        if (!hasText(value)) {
            return defaultValue;
        }
        try {
            return Integer.valueOf(value.replace(",", "").trim());
        } catch (NumberFormatException e) {
            throw new MarketApiException("정수 필드를 해석하지 못했습니다: " + value, e);
        }
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (hasText(value)) {
                return value.trim();
            }
        }
        return null;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
