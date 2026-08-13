package org.tajiro.location.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.tajiro.common.api.ApiResponse;
import org.tajiro.location.dto.AddressResolveResult;
import org.tajiro.location.service.KakaoLocalClient;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
@Api(tags = "위치", description = "주소 검색 관련 API")
public class AddressController {
    private final KakaoLocalClient kakaoLocalClient;

    @GetMapping("/addresses")
    @ApiOperation(
            value = "주소 검색",
            notes = "도로명·지번으로 먼저 찾고, 결과가 없으면 건물명으로 검색한다.\n"
                    + "아파트 여부와 동 목록은 건축물대장 연동 후 채워진다.")
    public ResponseEntity<ApiResponse<List<AddressResolveResult>>> searchAddress(
            @ApiParam(value="검색어", example = "자양동 51-5")
            @RequestParam("query") String query){
        return ResponseEntity.ok(ApiResponse.success(kakaoLocalClient.searchAddress(query)));
    }
}
