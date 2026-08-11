package org.tajiro.property.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertySafetyDTO {

    private Long propertyId;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private int crimeSafetyCount;
    private int trafficSafetyCount;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt; // 화면 상단 표출용 최상위 업데이트 일시

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

        @JsonIgnore // 각 항목 내부 응답에서는 제외
        private LocalDateTime updatedAt;

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

        @JsonRawValue
        private String polygon;
    }
}