package org.tajiro.seller.service;

import java.math.BigDecimal;

/**
 * 주소 담당 기능과 매물 등록 기능 사이의 연동 계약.
 *
 * <p>주소를 건물로 해석하지 못한 경우에는 예외 대신 {@code null}을 반환한다.</p>
 */
public interface BuildingResolveService {

    Long resolveBuildingId(
            String roadAddress,
            String buildingName,
            String dongName,
            BigDecimal latitude,
            BigDecimal longitude
    );
}
