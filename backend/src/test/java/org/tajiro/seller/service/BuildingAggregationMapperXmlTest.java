package org.tajiro.seller.service;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BuildingAggregationMapperXmlTest {

    private static final String RESOURCE =
            "org/tajiro/seller/mapper/BuildingAggregationMapper.xml";
    private static final String NAMESPACE =
            "org.tajiro.seller.mapper.BuildingAggregationMapper.";

    @Test
    void mapperXmlDefinesEveryStatusTransitionStatement() {
        Configuration configuration = new Configuration();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(RESOURCE)) {
            assertNotNull(input, "BuildingAggregationMapper.xml을 찾을 수 없습니다.");
            XMLMapperBuilder parser = new XMLMapperBuilder(
                    input,
                    configuration,
                    RESOURCE,
                    configuration.getSqlFragments()
            );
            parser.parse();
        } catch (Exception exception) {
            throw new AssertionError("BuildingAggregationMapper.xml 파싱에 실패했습니다.", exception);
        }

        List<String> statements = List.of(
                "findPendingInfrastructureBuildingIds",
                "findPendingSafetyBuildingIds",
                "claimInfrastructure",
                "completeInfrastructure",
                "failInfrastructure",
                "claimSafety",
                "completeSafety",
                "failSafety"
        );
        for (String statement : statements) {
            assertTrue(
                    configuration.hasStatement(NAMESPACE + statement),
                    statement + " 구문이 없습니다."
            );
        }
    }
}
