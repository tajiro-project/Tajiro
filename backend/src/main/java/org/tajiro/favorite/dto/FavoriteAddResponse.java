package org.tajiro.favorite.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class FavoriteAddResponse {
    private Long favoriteId;
    private LocalDateTime createdAt;
}
