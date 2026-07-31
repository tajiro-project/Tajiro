package org.tajiro.property.mapper;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.tajiro.config.RootConfig;
import org.tajiro.property.domain.PropertyVO;
import org.tajiro.property.dto.PropertySearchRequest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { RootConfig.class })
@Log4j2
public class PropertyMapperTest {

    @Autowired
    private PropertyMapper mapper;

    @Test
    @DisplayName("Get Recommended Property List")
    public void getList() {
        PropertySearchRequest request = PropertySearchRequest.builder()
                .tradeTypes(new ArrayList<>(List.of("월세")))
                .propertyTypes(new ArrayList<>(List.of("원룸", "오피스텔")))
                .minDeposit(0)
                .maxDeposit(5000)
                .minMonthlyRent(0)
                .maxMonthlyRent(100)
                .minAreaM2(new BigDecimal("10"))
                .maxAreaM2(new BigDecimal("50"))
                .floorPreference(new ArrayList<>(List.of("1층", "2층 이상")))
                .workLat(new BigDecimal("36.33557"))
                .workLng(new BigDecimal("127.45991"))
                .maxWorkplaceDistanceMeters(5000)
                .build();
        for(PropertyVO vo : mapper.getList(request)) {
            log.info(vo);
        }
    }

    @Test
    @DisplayName("Get Recommended Property List(except filter)")
    public void getAll() {
        List<PropertyVO> list = mapper.getList(PropertySearchRequest.builder().build());
        log.info("총 {}건", list.size());
        log.info(list.get(0));
    }
}
