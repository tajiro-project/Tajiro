package org.tajiro.seller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.tajiro.seller.domain.SellerPropertyCountsVO;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerPropertyCounts {
    private long all;
    private long open;
    private long done;
    private long favorites;

    public static SellerPropertyCounts from(SellerPropertyCountsVO vo) {
        return SellerPropertyCounts.builder()
                .all(vo.getAll())
                .open(vo.getOpen())
                .done(vo.getDone())
                .favorites(vo.getFavorites())
                .build();
    }
}
