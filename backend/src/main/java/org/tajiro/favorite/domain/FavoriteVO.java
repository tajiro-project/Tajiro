package org.tajiro.favorite.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class FavoriteVO {
    private Long id;
    private Long userId;
    private Long propertyId;
    private LocalDateTime createdAt;
}
