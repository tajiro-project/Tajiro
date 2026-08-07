package org.tajiro.property.mapper;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.tajiro.config.RootConfig;
import org.tajiro.config.TestConfig;
import org.tajiro.property.dto.PropertyInfrastructureDTO;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { RootConfig.class, TestConfig.class })
@Log4j2
class InfrastructureMapperTest {

    @Autowired
    private InfrastructureMapper infrastructureMapper;

    @Test
    @DisplayName("건물 인프라 점 조회")
    void selectInfraPointsByBuildingId() {
        List<PropertyInfrastructureDTO.InfrastructureInfoDTO> list =
                infrastructureMapper.selectInfraPointsByBuildingId(26L);

        list.forEach(i -> log.info(String.format("%s %s %dm %d분 (%s, %s)",
                i.getInfraCategory(), i.getInfraName(), i.getDistanceM(),
                i.getWalkMinutes(), i.getLatitude(), i.getLongitude())));

        assertFalse(list.isEmpty());

        Map<String, Long> byCategory = list.stream().collect(
                Collectors.groupingBy(PropertyInfrastructureDTO.InfrastructureInfoDTO::getInfraCategory, Collectors.counting()));
        assertTrue(byCategory.values().stream().allMatch(c -> c <= 3));
    }
}