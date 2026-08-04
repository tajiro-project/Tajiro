package org.tajiro.property.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.tajiro.property.domain.PropertyDetailVO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class PropertyDetailDTO {

    private Long id;
    private String title;
    private String propertyType;
    private String tradeType;
    private Integer deposit;
    private Integer monthlyRent;
    private Integer maintenanceFee;
    private BigDecimal areaM2;
    private String floorInfo;
    private String address;
    private String dong;
    private String propertyDescription;
    private LocalDateTime availableDate;
    private Boolean discussionStatus;
    private Integer evaluationScore;

    // 1:N 관계 데이터
    private List<String> images;
    private List<InfraSummaryDTO> infraSummary;

    // VO -> DTO 변환 정적 팩토리 메서드
    public static PropertyDetailDTO of(PropertyDetailVO vo, List<String> images, List<InfraSummaryDTO> infraSummary) {
        if (vo == null) {
            return null;
        }

        return PropertyDetailDTO.builder()
                .id(vo.getId())
                .title(vo.getTitle())
                .propertyType(vo.getPropertyType())
                .tradeType(vo.getTradeType())
                .deposit(vo.getDeposit())
                .monthlyRent(vo.getMonthlyRent())
                .maintenanceFee(vo.getMaintenanceFee())
                .areaM2(vo.getAreaM2())
                .floorInfo(vo.getFloorInfo())
                .address(vo.getAddress())
                .dong(vo.getDong())
                .propertyDescription(vo.getPropertyDescription())
                .availableDate(vo.getAvailableDate())
                .discussionStatus(vo.getDiscussionStatus())
                .evaluationScore(vo.getEvaluationScore())
                .images(images)
                .infraSummary(infraSummary)
                .build();
    }

    // 인프라 요약 DTO
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Data
    public static class InfraSummaryDTO {
        private String category;
        private Integer count;
    }
}