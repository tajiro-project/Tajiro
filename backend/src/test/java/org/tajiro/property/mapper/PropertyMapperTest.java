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
import org.tajiro.preference.domain.HousingPreferenceVO;
import org.tajiro.preference.mapper.HousingPreferenceMapper;
import org.tajiro.property.domain.PropertyVO;
import org.tajiro.preference.dto.PropertySearchRequest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { RootConfig.class, TestConfig.class })
@Log4j2
public class PropertyMapperTest {

    @Autowired
    private PropertyMapper propertyMapper;

    @Autowired
    private HousingPreferenceMapper housingPreferenceMapper;

    @Test
    @DisplayName("지역 검색 경로")
    public void getListBySearch() {
        PropertySearchRequest request = PropertySearchRequest.builder()
                .userId(1L)
                .refLat(new BigDecimal("36.33557"))
                .refLng(new BigDecimal("127.45991"))
                .radiusMeters(1500)
                .build();

        List<PropertyVO> list = propertyMapper.getList(request);

        log.info("반경 1.5km : " + list.size() + "건");

        assertFalse(list.isEmpty());
        assertTrue(list.stream().allMatch(p->p.getDistanceMeters() <= 1500));
        assertTrue(list.stream().allMatch(p -> p.getDesiredInfraCount() == 0));
    }

    @Test
    @DisplayName("가치관 경로")
    public void getListByPreference() {
        HousingPreferenceVO pref = housingPreferenceMapper.findByUserId(2L);
        assertNotNull(pref);

        PropertySearchRequest request = new PropertySearchRequest().builder()
                .userId(2L)
                .refLat(pref.getWorkplaceLatitude())
                .refLng(pref.getWorkplaceLongitude())
                .maxWorkplaceDistanceMeters(pref.getMaxWorkplaceDistanceMeters())
                .propertyTypes(split(pref.getHousingTypes()))
                .tradeTypes(split(pref.getTradeTypes()))
                .floorPreference(split(pref.getFloorPreference()))
                .minDeposit(pref.getMinDeposit())
                .maxDeposit(pref.getMaxDeposit())
                .minMonthlyRent(pref.getMinMonthlyRent())
                .maxMonthlyRent(pref.getMaxMonthlyRent())
                .minSellingPrice(pref.getMinSellingPrice())
                .maxSellingPrice(pref.getMaxSellingPrice())
                .minAreaM2(pref.getMinArea())
                .maxAreaM2(pref.getMaxArea())
                .desiredInfraCategories(pref.getDesiredInfraCategories())
                .desiredAmenityCategories(pref.getDesiredAmenityCategories())
                .build();

        List<PropertyVO> list = propertyMapper.getList(request);

        list.forEach(p->System.out.printf(
                "%d %s %s %s %d/%d %s㎡ [%s] %dm 점수=%s 인프라=%d 편의=%d%n",
                p.getId(), p.getPropertyType(), p.getTradeType(), p.getFloorInfo(),
                p.getDeposit(), p.getMonthlyRent(), p.getAreaM2(),
                p.getBuildingVO().getBldNm(), p.getDistanceMeters(),
                p.getPropertyValueAnalysisResultVO() == null ? "없음" : p.getPropertyValueAnalysisResultVO().getRecommendScore(),
                p.getDesiredInfraCount(), p.getDesiredAmenityCount()));

        System.out.println("총 " + list.size() + "건");

        // userId : 1L 검증
//        assertTrue(list.stream().allMatch(p ->
//                p.getTradeType().equals("월세")
//                        && p.getDeposit() >= 500 && p.getDeposit() <= 3000
//                        && p.getMonthlyRent() >= 20 && p.getMonthlyRent() <= 65
//                        && p.getDistanceMeters() <= 2300));

        // userId : 2L 검증
        assertTrue(list.stream().allMatch(p ->
                (p.getTradeType().equals("월세") || p.getTradeType().equals("전세"))
                        && p.getDeposit() >= 1000
                        && p.getDeposit() <= 20000
                        && p.getAreaM2().compareTo(new BigDecimal("59")) >= 0
                        && p.getAreaM2().compareTo(new BigDecimal("112")) <= 0
                        && p.getDistanceMeters() <= 3000
        ));
    }

    @Test
    @DisplayName("점수 없는 매물 확인")
    void getListByNoRecommendScore() {
        PropertySearchRequest req = new PropertySearchRequest();
        req.setUserId(1L);
        req.setRefLat(new BigDecimal("36.3318"));
        req.setRefLng(new BigDecimal("127.4680"));

        List<PropertyVO> list = propertyMapper.getList(req);

        long hasRecommendScore = list.stream().filter(p -> p.getPropertyValueAnalysisResultVO() != null).count();
        System.out.println("점수 있음 " + hasRecommendScore + " / 전체 " + list.size());

        assertEquals(4, hasRecommendScore);
    }

    private List<String> split(String csv) {
        if (csv == null || csv.isBlank()) {
            return null;
        }

        return new ArrayList<>(
                Arrays.stream(csv.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toList()
        );
    }
}
