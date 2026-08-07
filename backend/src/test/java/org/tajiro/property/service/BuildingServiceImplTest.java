package org.tajiro.property.service;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.tajiro.config.RootConfig;
import org.tajiro.config.TestConfig;
import org.tajiro.property.dto.PropertyInfrastructureDTO.InfrastructureInfoDTO;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { RootConfig.class, TestConfig.class })
@Log4j2
class BuildingInfraServiceImplTest {

    @Autowired
    private BuildingInfraService buildingInfraService;

    @Test
    @DisplayName("건물 인프라 점 조회 - 카테고리별 3개 이하")
    void getInfraPoints() {
        List<InfrastructureInfoDTO> list = buildingInfraService.getInfraPoints(26L);

        list.forEach(i -> log.info(String.format("%s %s %dm %d분 (%s, %s)",
                i.getInfraCategory(), i.getInfraName(), i.getDistanceM(),
                i.getWalkMinutes(), i.getLatitude(), i.getLongitude())));
        log.info("총 " + list.size() + "건");

        assertFalse(list.isEmpty());

        assertTrue(list.stream().allMatch(
                i -> i.getLatitude() != null && i.getLongitude() != null));

        Map<String, Long> byCategory = list.stream().collect(Collectors.groupingBy(
                InfrastructureInfoDTO::getInfraCategory, Collectors.counting()));
        assertTrue(byCategory.values().stream().allMatch(c -> c <= 3));
    }

    @Test
    @DisplayName("인프라가 없는 건물은 빈 목록 - 예외를 던지지 않는다")
    void getInfraPointsWhenNotCollected() {
        List<InfrastructureInfoDTO> list = buildingInfraService.getInfraPoints(999L);

        assertNotNull(list);
        assertTrue(list.isEmpty());
    }
}