package org.tajiro.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class DashboardDTO {
    private String name;
    private String targetRegion;
    private LocalDate birthDate;
    private Integer favoriteCount;
    private Integer reportCount;
    private List<PriorityItem> priorities;

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Builder
    public static class PriorityItem {
        private String criterion;
        private Integer priorityOrder;
    }
}
