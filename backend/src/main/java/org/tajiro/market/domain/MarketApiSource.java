package org.tajiro.market.domain;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public enum MarketApiSource {
    APT_SALE_DETAIL(
            "https://apis.data.go.kr/1613000/RTMSDataSvcAptTradeDev/getRTMSDataSvcAptTradeDev",
            MarketPropertyCategory.APARTMENT,
            false,
            true),
    APT_SALE_BASIC(
            "https://apis.data.go.kr/1613000/RTMSDataSvcAptTrade/getRTMSDataSvcAptTrade",
            MarketPropertyCategory.APARTMENT,
            false,
            false),
    APT_RENT(
            "https://apis.data.go.kr/1613000/RTMSDataSvcAptRent/getRTMSDataSvcAptRent",
            MarketPropertyCategory.APARTMENT,
            true,
            true),
    OFFICETEL_SALE(
            "https://apis.data.go.kr/1613000/RTMSDataSvcOffiTrade/getRTMSDataSvcOffiTrade",
            MarketPropertyCategory.OFFICETEL,
            false,
            true),
    OFFICETEL_RENT(
            "https://apis.data.go.kr/1613000/RTMSDataSvcOffiRent/getRTMSDataSvcOffiRent",
            MarketPropertyCategory.OFFICETEL,
            true,
            true),
    ROW_HOUSE_SALE(
            "https://apis.data.go.kr/1613000/RTMSDataSvcRHTrade/getRTMSDataSvcRHTrade",
            MarketPropertyCategory.ROW_HOUSE,
            false,
            true),
    ROW_HOUSE_RENT(
            "https://apis.data.go.kr/1613000/RTMSDataSvcRHRent/getRTMSDataSvcRHRent",
            MarketPropertyCategory.ROW_HOUSE,
            true,
            true),
    SINGLE_HOUSE_SALE(
            "https://apis.data.go.kr/1613000/RTMSDataSvcSHTrade/getRTMSDataSvcSHTrade",
            MarketPropertyCategory.SINGLE_HOUSE,
            false,
            true),
    SINGLE_HOUSE_RENT(
            "https://apis.data.go.kr/1613000/RTMSDataSvcSHRent/getRTMSDataSvcSHRent",
            MarketPropertyCategory.SINGLE_HOUSE,
            true,
            true);

    private final String endpoint;
    private final MarketPropertyCategory propertyCategory;
    private final boolean rental;
    private final boolean defaultSource;

    MarketApiSource(
            String endpoint,
            MarketPropertyCategory propertyCategory,
            boolean rental,
            boolean defaultSource) {
        this.endpoint = endpoint;
        this.propertyCategory = propertyCategory;
        this.rental = rental;
        this.defaultSource = defaultSource;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public MarketPropertyCategory getPropertyCategory() {
        return propertyCategory;
    }

    public boolean isRental() {
        return rental;
    }

    public boolean isDefaultSource() {
        return defaultSource;
    }

    public static List<MarketApiSource> defaultsFor(
            MarketPropertyCategory category,
            MarketTradeType tradeType) {
        if (category == null || tradeType == null) {
            return Collections.emptyList();
        }
        boolean rental = tradeType != MarketTradeType.SALE;
        return Arrays.stream(values())
                .filter(MarketApiSource::isDefaultSource)
                .filter(source -> source.propertyCategory == category)
                .filter(source -> source.rental == rental)
                .collect(Collectors.toList());
    }
}
