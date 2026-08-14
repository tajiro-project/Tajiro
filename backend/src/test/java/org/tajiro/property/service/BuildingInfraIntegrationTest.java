package org.tajiro.property.service;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.tajiro.property.domain.InfrastructureVO;
import org.tajiro.property.mapper.InfraMapper;

// ⚠️ 본인 프로젝트의 Configuration 클래스를 import 해야 합니다.
import org.tajiro.config.RootConfig;
import org.tajiro.security.config.SecurityConfig;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        RootConfig.class,
        SecurityConfig.class
})
@TestPropertySource("classpath:application.properties")
@WebAppConfiguration
class BuildingInfraIntegrationTest {

    @Autowired
    private BuildingInfraBatchService buildingInfraBatchService;

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @Test
    @DisplayName("1. 신규 건물 1개에 대해 카카오 API 수집 및 DB 저장 검증")
    void testSingleBuildingInfraCollection() {
        // 1단계 쿼리에서 조회된 신규 건물 ID 중 하나를 넣어줍니다. (예: 101L)
        Long targetBuildingId = 53L;

        System.out.println("====== [단건] 카카오 API 호출 및 DB 저장 시작 ======");
        buildingInfraBatchService.processSingleBuilding(targetBuildingId);
        System.out.println("====== [단건] 카카오 API 호출 및 DB 저장 완료 ======");

        // DB에 정상 저장되었는지 수동 체크 및 검증
        try (SqlSession session = sqlSessionFactory.openSession()) {
            InfraMapper mapper = session.getMapper(InfraMapper.class);

            InfrastructureVO subwayInfra = mapper.selectInfrastructureByBuildingAndCategory(targetBuildingId, "subway");

            assertNotNull(subwayInfra, "DB에 지하철 인프라 정보가 저장되지 않았습니다!");
            System.out.println("▶ [건물 " + targetBuildingId + "] 저장된 지하철 개수: " + subwayInfra.getCountWithin2000m());
            System.out.println("▶ [건물 " + targetBuildingId + "] 가장 가까운 지하철역 거리: " + subwayInfra.getNearestDistanceMeters() + "m");
            System.out.println("▶ [건물 " + targetBuildingId + "] 예상 도보 시간: " + subwayInfra.getNearestWalkMinutes() + "분");
        }
    }

    @Test
    @DisplayName("2. 인프라 미수집 건물들 일괄 배치 처리 검증")
    void testBatchBuildingInfraCollection() {
        // 아직 인프라 수집이 안 된 건물 5개를 한 번에 수집하는 배치 테스트
        int batchSize = 5;

        System.out.println("====== [배치] 인프라 미수집 건물 " + batchSize + "개 수집 시작 ======");
        buildingInfraBatchService.processPendingBuildings(batchSize);
        System.out.println("====== [배치] 수집 완료 ======");
    }
}