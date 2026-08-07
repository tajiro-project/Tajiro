package org.tajiro.property.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.tajiro.common.api.ApiResponse;
import org.tajiro.common.api.ErrorCode;
import org.tajiro.exception.BusinessException;
import org.tajiro.property.dto.PropertyListDTO;
import org.tajiro.property.service.PropertyService;
import springfox.documentation.annotations.ApiIgnore;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/properties")
@RequiredArgsConstructor
@Api(tags = "매물")
public class PropertyController {
    private final PropertyService propertyService;

    @GetMapping
    @ApiOperation(
            value = "매물 목록 조회",
            notes = "좌표를 주면 그 지점 반경 1.5km 매물을 조회한다. 추천 점수는 내려가지 않는다.\n"
                    + "좌표가 없으면 저장된 주거 선호 조건으로 조회하며, 조건이 없으면 404 를 준다.")
    public ResponseEntity<ApiResponse<List<PropertyListDTO>>> getList(
            @ApiIgnore @AuthenticationPrincipal Long userId,

            @ApiParam(value = "검색 지점 위도", example = "36.33557")
            @RequestParam(required = false)BigDecimal centerLat,

            @ApiParam(value = "검색 지점 경도", example = "127.45991")
            @RequestParam(required = false)BigDecimal centerLng
            ) {
        requireUserId(userId);

        if((centerLat == null) != (centerLng == null)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        return ResponseEntity.ok(ApiResponse.success(propertyService.getList(userId, centerLat, centerLng)));
    }

    private Long requireUserId(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.AUTH_REQUIRED);
        }
        return userId;
    }
}
