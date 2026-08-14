package org.tajiro.property.service;

import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.tajiro.common.api.ErrorCode;
import org.tajiro.config.RootConfig;
import org.tajiro.config.TestConfig;
import org.tajiro.exception.BusinessException;
import org.tajiro.property.domain.PropertyVO;
import org.tajiro.property.dto.PropertyListDTO;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { RootConfig.class, TestConfig.class })
@Log4j2
class PropertyServiceImplTest {
    @Autowired
    private PropertyService propertyService;
    @Test
    @DisplayName("가치관 조건으로 매물 가져오기")
    void findMatchingProperties() {
        List<PropertyVO> list = propertyService.findMatchingProperties(2L);

        list.forEach(p -> log.info(String.format(
                "%d %s %s %s %d/%d %s㎡ %dm",
                p.getId(), p.getPropertyType(), p.getTradeType(), p.getFloorInfo(),
                p.getDeposit(), p.getMonthlyRent(), p.getAreaM2(), p.getDistanceMeters())));
        log.info("user 2 조건 통과 " + list.size() + "건");

        assertFalse(list.isEmpty());

        assertTrue(list.stream().allMatch(p ->
                (p.getTradeType().equals("월세") || p.getTradeType().equals("전세"))
                        && p.getDeposit() >= 1000
                        && p.getDeposit() <= 20000
                        && p.getAreaM2().compareTo(new BigDecimal("59")) >= 0
                        && p.getAreaM2().compareTo(new BigDecimal("112")) <= 0
                        && p.getDistanceMeters() <= 3000));
    }

    @Test
    @DisplayName("가치관이 없으면 PREFERENCE_NOT_FOUND")
    public void findMatchingPropertiesWithoutPreference() {
        BusinessException e = assertThrows(BusinessException.class,
                () -> propertyService.findMatchingProperties(999L));

        assertEquals(ErrorCode.PREFERENCE_NOT_FOUND, e.getResponseCode());
    }

    @Test
    @DisplayName("지역 검색 경로 - 반경 1.5km, 점수 없음")
    void getListByRegion() {
        List<PropertyListDTO> list = propertyService.getList(
                1L);

        log.info("반경 1.5km : " + list.size() + "건");

        assertFalse(list.isEmpty());
        assertTrue(list.stream().allMatch(p -> p.getDistanceMeters() <= 1500));

        assertTrue(list.stream().allMatch(p -> p.getRecommendScore() == null));

        assertTrue(list.stream().allMatch(p -> p.getDesiredInfraCount() == 0));
        assertTrue(list.stream().allMatch(p -> p.getDesiredAmenityCount() == 0));
    }

    @Test
    @DisplayName("가치관 경로 - 응답 변환 확인")
    void getListByPreference() {
        List<PropertyListDTO> list = propertyService.getList(2L);

        log.info("user 2 : " + list.size() + "건");
        assertFalse(list.isEmpty());

        PropertyListDTO first = list.get(0);
        log.info(first);

        list.forEach(p -> log.info(String.format(
                "%d %s %s %d/%d %s㎡ %dm 인프라=%d 편의=%d 점수=%s",
                p.getId(), p.getPropertyType(), p.getTradeType(),
                p.getDeposit(), p.getMonthlyRent(), p.getAreaM2(),
                p.getDistanceMeters(),
                p.getDesiredInfraCount(), p.getDesiredAmenityCount(),
                p.getRecommendScore())));

        assertTrue(list.stream().allMatch(p -> p.getRecommendScore() != null));
        assertTrue(list.stream().allMatch(p ->
                p.getRecommendScore() >= 0 && p.getRecommendScore() <= 100));

        assertNotNull(first.getDistanceMeters());
        assertNotNull(first.getDesiredInfraCount());
        assertNotNull(first.getDesiredAmenityCount());

        assertNotNull(first.getLatitude());
        assertNotNull(first.getLongitude());
        assertNotNull(first.getBuildingName());
    }

    @Test
    @DisplayName("좌표도 가치관도 없으면 PREFERENCE_NOT_FOUND")
    void getListWithoutCoordinatesAndPreference() {
        BusinessException e = assertThrows(BusinessException.class,
                () -> propertyService.getList(999L));

        assertEquals(ErrorCode.PREFERENCE_NOT_FOUND, e.getResponseCode());
    }
}