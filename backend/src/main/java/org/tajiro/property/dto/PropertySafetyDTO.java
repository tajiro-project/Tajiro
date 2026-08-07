package org.tajiro.property.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertySafetyDTO {

    private Long propertyId;
    private BigDecimal latitude;   // 매물(건물) 위도
    private BigDecimal longitude;  // 매물(건물) 경도
    private int crimeSafetyCount;
    private int trafficSafetyCount;
    private List<SafetyItemDTO> safetyList;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SafetyItemDTO {
        private Long safeId;
        private String safeCategory;
        private Integer countWithin500m;
        private Integer nearestDistanceMeters;
        private String nearestSafeName;
        private List<SafetyDetailDTO> details;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SafetyDetailDTO {
        private Long safeDetailId;
        private String safeName;
        private BigDecimal latitude;
        private BigDecimal longitude;
        private Integer distanceM;
        private Object polygon; // 폴리곤 JSON 데이터
    }
}