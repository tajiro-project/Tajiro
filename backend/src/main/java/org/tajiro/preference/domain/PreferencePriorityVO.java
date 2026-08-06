package org.tajiro.preference.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreferencePriorityVO {
    private Long id;
    private Long userId;
    private String criterion;
    private Integer priorityOrder;
}
