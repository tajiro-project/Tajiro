package org.tajiro.market.service;

import org.junit.jupiter.api.Test;
import org.tajiro.market.domain.MarketPropertyCategory;
import org.tajiro.market.domain.MarketPropertyVO;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MarketPropertyCategoryResolverTest {

    private final MarketPropertyCategoryResolver resolver = new MarketPropertyCategoryResolver();

    @Test
    void houseVillaRentMapsToRowHouseAndSingleHouse() {
        List<MarketPropertyCategory> categories = resolver.resolve(property("\uC8FC\uD0DD/\uBE4C\uB77C", "\uC804\uC138"));

        assertEquals(Arrays.asList(
                MarketPropertyCategory.ROW_HOUSE,
                MarketPropertyCategory.SINGLE_HOUSE), categories);
    }

    @Test
    void houseVillaSaleExcludesSingleHouseSale() {
        List<MarketPropertyCategory> categories = resolver.resolve(property("\uC8FC\uD0DD/\uBE4C\uB77C", "\uB9E4\uB9E4"));

        assertEquals(Arrays.asList(
                MarketPropertyCategory.ROW_HOUSE,
                MarketPropertyCategory.OFFICETEL), categories);
    }

    @Test
    void oneRoomRentMapsToRowHouseOfficetelAndSingleHouse() {
        List<MarketPropertyCategory> categories = resolver.resolve(property("\uC6D0\uB8F8", "\uC6D4\uC138"));

        assertEquals(Arrays.asList(
                MarketPropertyCategory.ROW_HOUSE,
                MarketPropertyCategory.OFFICETEL,
                MarketPropertyCategory.SINGLE_HOUSE), categories);
    }

    @Test
    void oneRoomSaleExcludesSingleHouseSale() {
        List<MarketPropertyCategory> categories = resolver.resolve(property("\uC6D0\uB8F8", "\uB9E4\uB9E4"));

        assertEquals(Arrays.asList(
                MarketPropertyCategory.ROW_HOUSE,
                MarketPropertyCategory.OFFICETEL), categories);
    }

    private MarketPropertyVO property(String propertyType, String tradeType) {
        return MarketPropertyVO.builder()
                .propertyType(propertyType)
                .tradeType(tradeType)
                .build();
    }
}