package org.tajiro.preference.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreferenceDTO {
    private Workplace workplace;
    private Boolean hasCar;
    private Integer maxCommuteDistanceMeters;
    private List<String> housingTypes;
    private List<String> tradeTypes;
    private List<Integer> depositJeonseRange;
    private List<Integer> monthlyRentRange;
    private List<Integer> salePriceRange;
    private List<BigDecimal> areaRange;
    private List<String> floorPreference;
    private List<String> desiredInfraCategories;

    @JsonAlias("desiredAmenityCategories")
    private List<String> desiredAmenityCategories;

    private List<Priority> priorities;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Workplace {
        private String name;
        private String address;
        private BigDecimal lat;
        private BigDecimal lng;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Priority {
        private String criterion;
        private Integer priorityOrder;
    }
}
