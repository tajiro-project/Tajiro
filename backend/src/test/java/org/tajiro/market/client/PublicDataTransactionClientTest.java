package org.tajiro.market.client;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;
import org.tajiro.market.domain.ActualTransactionVO;
import org.tajiro.market.domain.MarketApiSource;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PublicDataTransactionClientTest {

    @Test
    void decodingKeyIsEncodedExactlyOnce() {
        PublicDataTransactionClient client = new PublicDataTransactionClient(
                new RestTemplate(),
                new PublicDataXmlParser(),
                "abc+def/ghi==",
                "",
                false,
                1000);

        URI uri = client.buildUri(
                MarketApiSource.APT_SALE_DETAIL,
                "30110",
                "202608",
                1);

        assertTrue(uri.getRawQuery().contains(
                "serviceKey=abc%2Bdef%2Fghi%3D%3D"));
    }

    @Test
    void encodingKeyIsNotDoubleEncoded() {
        PublicDataTransactionClient client = new PublicDataTransactionClient(
                new RestTemplate(),
                new PublicDataXmlParser(),
                "abc%2Bdef%2Fghi%3D%3D",
                "",
                true,
                1000);

        URI uri = client.buildUri(
                MarketApiSource.APT_SALE_DETAIL,
                "30110",
                "202608",
                1);

        assertTrue(uri.getRawQuery().contains(
                "serviceKey=abc%2Bdef%2Fghi%3D%3D"));
    }

    @Test
    void identicalPublicRowsAreKeptWithDifferentOccurrenceKeys() {
        PublicDataTransactionClient client = new PublicDataTransactionClient(
                new RestTemplate(),
                new PublicDataXmlParser(),
                "test-key",
                "",
                false,
                1000);
        ActualTransactionVO first = new ActualTransactionVO();
        first.setSourceUniqueKey("same-public-fields");
        ActualTransactionVO second = new ActualTransactionVO();
        second.setSourceUniqueKey("same-public-fields");

        List<ActualTransactionVO> rows = Arrays.asList(first, second);
        client.prepareSnapshotRows(rows, "202608");

        assertEquals("202608", rows.get(0).getSourceDealYm());
        assertEquals("202608", rows.get(1).getSourceDealYm());
        assertNotEquals(rows.get(0).getSourceUniqueKey(), rows.get(1).getSourceUniqueKey());
    }
}
