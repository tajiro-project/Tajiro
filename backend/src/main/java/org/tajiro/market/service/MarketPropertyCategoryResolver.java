package org.tajiro.market.service;

import org.springframework.stereotype.Component;
import org.tajiro.market.domain.MarketPropertyCategory;
import org.tajiro.market.domain.MarketPropertyVO;
import org.tajiro.market.domain.MarketSyncTarget;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class MarketPropertyCategoryResolver {

    private static final String APARTMENT = "\uC544\uD30C\uD2B8";
    private static final String OFFICETEL = "\uC624\uD53C\uC2A4\uD154";
    private static final String HOUSE_VILLA = "\uC8FC\uD0DD/\uBE4C\uB77C";
    private static final String ONE_ROOM = "\uC6D0\uB8F8";
    private static final String TWO_ROOM = "\uD22C\uB8F8";
    private static final String SALE = "\uB9E4\uB9E4";

    public List<MarketPropertyCategory> resolve(MarketSyncTarget target) {
        MarketPropertyCategory configured = configured(target.getConfiguredCategory());
        if (configured != null) {
            return Collections.singletonList(configured);
        }
        return defaultsForPropertyType(target.getPropertyType(), target.getTradeType());
    }

    public List<MarketPropertyCategory> resolve(MarketPropertyVO property) {
        MarketPropertyCategory configured = configured(property.getConfiguredCategory());
        if (configured != null) {
            return Collections.singletonList(configured);
        }
        return defaultsForPropertyType(property.getPropertyType(), property.getTradeType());
    }

    private List<MarketPropertyCategory> defaultsForPropertyType(String propertyType, String tradeType) {
        if (APARTMENT.equals(propertyType)) {
            return Collections.singletonList(MarketPropertyCategory.APARTMENT);
        }
        if (OFFICETEL.equals(propertyType)) {
            return Collections.singletonList(MarketPropertyCategory.OFFICETEL);
        }

        boolean sale = SALE.equals(tradeType);
        if (HOUSE_VILLA.equals(propertyType)) {
            if (sale) {
                return Arrays.asList(MarketPropertyCategory.ROW_HOUSE, MarketPropertyCategory.OFFICETEL);
            }
            return Arrays.asList(MarketPropertyCategory.ROW_HOUSE, MarketPropertyCategory.SINGLE_HOUSE);
        }
        if (ONE_ROOM.equals(propertyType) || TWO_ROOM.equals(propertyType)) {
            Set<MarketPropertyCategory> categories = new LinkedHashSet<>();
            categories.add(MarketPropertyCategory.ROW_HOUSE);
            categories.add(MarketPropertyCategory.OFFICETEL);
            if (!sale) {
                categories.add(MarketPropertyCategory.SINGLE_HOUSE);
            }
            return categories.stream().collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private MarketPropertyCategory configured(String configuredCategory) {
        if (configuredCategory == null || configuredCategory.trim().isEmpty()) {
            return null;
        }
        try {
            return MarketPropertyCategory.valueOf(configuredCategory.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}