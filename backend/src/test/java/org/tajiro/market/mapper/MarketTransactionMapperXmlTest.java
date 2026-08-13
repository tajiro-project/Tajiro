package org.tajiro.market.mapper;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

import java.io.Reader;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MarketTransactionMapperXmlTest {

    @Test
    void mapperXmlLoadsAllStatements() throws Exception {
        String resource = "org/tajiro/market/mapper/MarketTransactionMapper.xml";
        Configuration configuration = new Configuration();

        try (Reader reader = Resources.getResourceAsReader(resource)) {
            new XMLMapperBuilder(reader, configuration, resource, configuration.getSqlFragments())
                    .parse();
        }

        String namespace = MarketTransactionMapper.class.getName() + ".";
        assertTrue(configuration.hasStatement(namespace + "deleteTransactionsForMonth"));
        assertTrue(configuration.hasStatement(namespace + "deleteTransactionsBefore"));
        assertTrue(configuration.hasStatement(namespace + "deleteCoverageBefore"));
        assertTrue(configuration.hasStatement(namespace + "upsertTransactions"));
        assertTrue(configuration.hasStatement(namespace + "hasSuccessfulCoverage"));
        assertTrue(configuration.hasStatement(namespace + "findComparableTransactions"));
        assertTrue(configuration.hasStatement(namespace + "updateEvaluation"));
    }
}
