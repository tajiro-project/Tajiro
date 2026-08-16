package org.tajiro.seller.domain;

import lombok.Data;

@Data
public class SellerPropertyCountsVO {
    private long all;
    private long open;
    private long done;
    private long favorites;
}
