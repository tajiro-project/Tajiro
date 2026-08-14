package org.tajiro.market.client;

import org.junit.jupiter.api.Test;
import org.tajiro.market.domain.ActualTransactionVO;
import org.tajiro.market.domain.MarketApiSource;
import org.tajiro.market.domain.MarketPropertyCategory;
import org.tajiro.market.domain.MarketTradeType;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PublicDataXmlParserTest {

    private final PublicDataXmlParser parser = new PublicDataXmlParser();

    @Test
    void parsesApartmentDetailedSale() {
        String xml = response(
                "<aptDong>107</aptDong>"
                        + "<aptNm>인왕산아이파크</aptNm>"
                        + "<aptSeq>11110-2212</aptSeq>"
                        + "<buildYear>2008</buildYear>"
                        + "<dealAmount>143,500</dealAmount>"
                        + "<dealDay>17</dealDay><dealMonth>7</dealMonth><dealYear>2024</dealYear>"
                        + "<excluUseAr>114.931</excluUseAr><floor>1</floor>"
                        + "<jibun>60</jibun><sggCd>11110</sggCd>"
                        + "<umdCd>18700</umdCd><umdNm>무악동</umdNm>"
                        + "<cdealType></cdealType><cdealDay></cdealDay>");

        MarketApiPage page = parser.parse(
                MarketApiSource.APT_SALE_DETAIL,
                "11110",
                xml);
        ActualTransactionVO item = page.getItems().get(0);

        assertEquals(MarketPropertyCategory.APARTMENT, item.getPropertyCategory());
        assertEquals(MarketTradeType.SALE, item.getTradeType());
        assertEquals(143_500, item.getDealAmount());
        assertEquals(new BigDecimal("114.931"), item.getExclusiveAreaM2());
        assertEquals("11110-2212", item.getSourceBuildingCode());
        assertFalse(item.getCanceled());
        assertEquals(64, item.getSourceUniqueKey().length());
    }

    @Test
    void parsesOfficetelMonthlyRent() {
        String xml = response(
                "<offiNm>더넥스트 종로</offiNm>"
                        + "<deposit>1,000</deposit><monthlyRent>65</monthlyRent>"
                        + "<dealDay>21</dealDay><dealMonth>12</dealMonth><dealYear>2025</dealYear>"
                        + "<excluUseAr>26.52</excluUseAr><floor>11</floor>"
                        + "<jibun>206</jibun><sggCd>11110</sggCd><umdNm>연건동</umdNm>");

        ActualTransactionVO item = parser.parse(
                        MarketApiSource.OFFICETEL_RENT,
                        "11110",
                        xml)
                .getItems().get(0);

        assertEquals(MarketPropertyCategory.OFFICETEL, item.getPropertyCategory());
        assertEquals(MarketTradeType.MONTHLY_RENT, item.getTradeType());
        assertEquals(1_000, item.getDeposit());
        assertEquals(65, item.getMonthlyRent());
        assertEquals("더넥스트 종로", item.getBuildingName());
    }

    @Test
    void parsesRowHouseAndSingleHouseSpecificAreas() {
        String rowHouseXml = response(
                "<mhouseNm>21스타캐슬</mhouseNm><houseType>다세대</houseType>"
                        + "<dealAmount>47,000</dealAmount>"
                        + "<dealDay>24</dealDay><dealMonth>1</dealMonth><dealYear>2025</dealYear>"
                        + "<excluUseAr>59.75</excluUseAr><landAr>40.93</landAr>"
                        + "<floor>3</floor><jibun>581</jibun><sggCd>11110</sggCd><umdNm>숭인동</umdNm>");
        String singleHouseXml = response(
                "<houseType>단독</houseType><dealAmount>80,000</dealAmount>"
                        + "<dealDay>3</dealDay><dealMonth>2</dealMonth><dealYear>2025</dealYear>"
                        + "<totalFloorAr>120.5</totalFloorAr><plottageAr>92.4</plottageAr>"
                        + "<jibun>12-3</jibun><sggCd>11110</sggCd><umdNm>평창동</umdNm>");

        ActualTransactionVO rowHouse = parser.parse(
                        MarketApiSource.ROW_HOUSE_SALE,
                        "11110",
                        rowHouseXml)
                .getItems().get(0);
        ActualTransactionVO singleHouse = parser.parse(
                        MarketApiSource.SINGLE_HOUSE_SALE,
                        "11110",
                        singleHouseXml)
                .getItems().get(0);

        assertEquals("21스타캐슬", rowHouse.getBuildingName());
        assertEquals(new BigDecimal("40.93"), rowHouse.getLandAreaM2());
        assertEquals(new BigDecimal("120.5"), singleHouse.getTotalFloorAreaM2());
        assertEquals(new BigDecimal("92.4"), singleHouse.getLandAreaM2());
    }

    @Test
    void marksCanceledSaleAndRejectsApiErrors() {
        String canceledXml = response(
                "<aptNm>테스트아파트</aptNm><dealAmount>50,000</dealAmount>"
                        + "<dealDay>1</dealDay><dealMonth>1</dealMonth><dealYear>2025</dealYear>"
                        + "<excluUseAr>84.9</excluUseAr><sggCd>11110</sggCd>"
                        + "<cdealType>O</cdealType><cdealDay>25.02.03</cdealDay>");

        ActualTransactionVO canceled = parser.parse(
                        MarketApiSource.APT_SALE_DETAIL,
                        "11110",
                        canceledXml)
                .getItems().get(0);
        assertTrue(canceled.getCanceled());

        String errorXml = "<OpenAPI_ServiceResponse><cmmMsgHeader>"
                + "<errMsg>SERVICE ERROR</errMsg>"
                + "<returnAuthMsg>SERVICE_KEY_IS_NOT_REGISTERED_ERROR</returnAuthMsg>"
                + "</cmmMsgHeader></OpenAPI_ServiceResponse>";
        MarketApiException exception = assertThrows(
                MarketApiException.class,
                () -> parser.parse(MarketApiSource.APT_RENT, "11110", errorXml));
        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("SERVICE_KEY_IS_NOT_REGISTERED_ERROR"));
    }

    @Test
    void declaresAllNineApprovedApiSources() {
        assertEquals(9, MarketApiSource.values().length);
        assertFalse(MarketApiSource.APT_SALE_BASIC.isDefaultSource());
        assertTrue(MarketApiSource.APT_SALE_DETAIL.isDefaultSource());
    }

    private String response(String item) {
        return "<response><header><resultCode>000</resultCode><resultMsg>OK</resultMsg></header>"
                + "<body><items><item>" + item + "</item></items>"
                + "<numOfRows>100</numOfRows><pageNo>1</pageNo><totalCount>1</totalCount>"
                + "</body></response>";
    }
}
