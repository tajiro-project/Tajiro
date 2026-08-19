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

    // 1. 기존 매물 정보 필드
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
    private Integer roomNum;
    private Integer bathroomNum;
    private Boolean parkAvailability;
    private String propertyDescription;
    private LocalDateTime moveInDate;
    private LocalDateTime availableDate;
    private Boolean discussionStatus;

    // ✨ [수정] 1/0 DB 값을 Boolean(true/false)으로 받도록 변경
    private Boolean transactionStatus; // property.transaction_status (1: true, 0: false)
    private LocalDateTime deletedAt;    // property.deleted_at

    // 중개사 정보
    private BigDecimal evaluationScore;
    private String agentPhone;
    private String agencyName;

    // 2. 분석 및 상태 정보
    private Integer recommendScore;
    private Integer workplaceDistanceMeters;
    private Boolean isFavorite;

    // 3. 연관 목록 및 기타 정보
    private List<String> images;
    private List<InfraSummaryDTO> infraSummary;
    private String sigunguCd;

    // 팩토리 메서드 (of)
    public static PropertyDetailDTO of(
            PropertyDetailVO vo,
            List<String> images,
            List<InfraSummaryDTO> infraSummary,
            Boolean isFavorite,
            Integer recommendScore,
            Integer workplaceDistanceMeters
    ) {
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
                .roomNum(vo.getRoomNum())
                .bathroomNum(vo.getBathroomNum())
                .parkAvailability(vo.getParkAvailability())
                .propertyDescription(vo.getPropertyDescription())
                .moveInDate(vo.getMoveInDate())
                .availableDate(vo.getAvailableDate())
                .discussionStatus(vo.getDiscussionStatus())
                .transactionStatus(vo.getTransactionStatus()) // Boolean 값 매핑
                .deletedAt(vo.getDeletedAt())
                .evaluationScore(vo.getEvaluationScore())
                .agentPhone(vo.getAgentPhone())
                .agencyName(vo.getAgencyName())
                .recommendScore(recommendScore)
                .workplaceDistanceMeters(workplaceDistanceMeters)
                .isFavorite(isFavorite)
                .images(images)
                .infraSummary(infraSummary)
                .sigunguCd(vo.getSigunguCd())
                .build();
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Data
    public static class InfraSummaryDTO {
        private String category;
        private Integer count;
    }
}