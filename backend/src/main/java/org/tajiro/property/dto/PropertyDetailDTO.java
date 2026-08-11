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
    private Integer roomNum;
    private Integer bathroomNum;
    private Boolean parkAvailability;
    private String propertyDescription;
    private LocalDateTime moveInDate;
    private LocalDateTime availableDate;
    private Boolean discussionStatus;

    private Integer recommendScore; // API 응답 데이터
    private Boolean isFavorite;     // API 응답 데이터

    private List<String> images;
    private List<InfraSummaryDTO> infraSummary;

    // ✨ recommendScore를 파라미터로 추가 수용
    public static PropertyDetailDTO of(
            PropertyDetailVO vo,
            List<String> images,
            List<InfraSummaryDTO> infraSummary,
            Boolean isFavorite,
            Integer recommendScore
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
                .recommendScore(recommendScore) // ✨ 전달받은 값 바인딩
                .isFavorite(isFavorite)
                .images(images)
                .infraSummary(infraSummary)
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