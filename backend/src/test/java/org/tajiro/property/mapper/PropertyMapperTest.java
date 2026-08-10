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
import org.tajiro.preference.mapper.PreferenceMapper;
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
    private PreferenceMapper housingPreferenceMapper;

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
        HousingPreferenceVO pref = housingPreferenceMapper.findByUserId(3L);
        assertNotNull(pref, "user 3 의 가치관이 없습니다");

        PropertySearchRequest request = PropertySearchRequest.builder()
                .userId(3L)
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
                .hasCar(pref.getHasCar())
                .build();

        List<PropertyVO> list = propertyMapper.getList(request);

        list.forEach(p->System.out.printf(
                "%d %s %s %s %d/%d %s㎡ 주차%s [%s] %dm 점수=%s 인프라=%d 편의=%d%n",
                p.getId(), p.getPropertyType(), p.getTradeType(), p.getFloorInfo(),
                p.getDeposit(), p.getMonthlyRent(), p.getAreaM2(),
                Boolean.TRUE.equals(p.getParkAvailability()) ? "O" : "X",
                p.getBuildingVO().getBldNm(), p.getDistanceMeters(),
                p.getPropertyValueAnalysisResultVO() == null ? "없음" : p.getPropertyValueAnalysisResultVO().getRecommendScore(),
                p.getDesiredInfraCount(), p.getDesiredAmenityCount()));

        System.out.println("총 " + list.size() + "건 (자차 보유 = " + pref.getHasCar() + ")");

        assertFalse(list.isEmpty());

        List<String> types  = split(pref.getHousingTypes());
        List<String> trades = split(pref.getTradeTypes());

        assertTrue(list.stream().allMatch(p ->
                types.contains(p.getPropertyType())
                        && trades.contains(p.getTradeType())
                        && p.getAreaM2().compareTo(pref.getMinArea()) >= 0
                        && p.getAreaM2().compareTo(pref.getMaxArea()) <= 0
                        && p.getDistanceMeters() <= pref.getMaxWorkplaceDistanceMeters()
        ));

        assertTrue(list.stream().allMatch(p -> {
            switch (p.getTradeType()) {
                case "월세":
                    return p.getDeposit() >= pref.getMinDeposit()
                            && p.getDeposit() <= pref.getMaxDeposit()
                            && p.getMonthlyRent() >= pref.getMinMonthlyRent()
                            && p.getMonthlyRent() <= pref.getMaxMonthlyRent();
                case "전세":
                    return p.getDeposit() >= pref.getMinDeposit()
                            && p.getDeposit() <= pref.getMaxDeposit();
                case "매매":
                    return p.getDeposit() >= pref.getMinSellingPrice()
                            && p.getDeposit() <= pref.getMaxSellingPrice();
                default:
                    return false;
            }
        }));

        if (Boolean.TRUE.equals(pref.getHasCar())) {
            assertTrue(list.stream().allMatch(p -> Boolean.TRUE.equals(p.getParkAvailability())));
        }
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
