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
import org.tajiro.location.service.BuildingLedgerClient;
import org.tajiro.location.service.KakaoLocalClient;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
@Api(tags = "위치", description = "주소 검색 관련 API")
public class AddressController {
    private final KakaoLocalClient kakaoLocalClient;
    private final BuildingLedgerClient buildingLedgerClient;


    @GetMapping("/addresses")
    @ApiOperation(
            value = "주소 검색",
            notes = "도로명·지번으로 먼저 찾고, 결과가 없으면 건물명으로 검색한다.\n"
                    + "아파트 여부와 동 목록은 건축물대장 연동 후 채워진다.")
    public ResponseEntity<ApiResponse<List<AddressResolveResult>>> searchAddress(
            @ApiParam(value="검색어", example = "현대 아파트")
            @RequestParam("query") String query){
        return ResponseEntity.ok(ApiResponse.success(kakaoLocalClient.searchAddress(query)));
    }


    @GetMapping("/buildings/dongs")
    @ApiOperation(
            value = "건물 동 목록 조회",
            notes = "주소 검색 결과의 시군구·법정동·본번·부번으로 건축물대장 표제부를 조회한다.\n"
                    + "동이 여러 개면 아파트, 비어 있으면 단일 건물이다.")
    public ResponseEntity<ApiResponse<List<String>>> searchDongs(
            @ApiParam(value = "시군구코드", example = "11680") @RequestParam String sigunguCd,
            @ApiParam(value = "법정동코드", example = "11000") @RequestParam String bjdongCd,
            @ApiParam(value = "대지구분. 0 대지 / 1 산", example = "0")
            @RequestParam(defaultValue = "0") String platGbCd,
            @ApiParam(value = "본번 4자리", example = "0426") @RequestParam String bun,
            @ApiParam(value = "부번 4자리", example = "0000") @RequestParam String ji) {

        AddressResolveResult address = AddressResolveResult.builder()
                .sigunguCd(sigunguCd)
                .bjdongCd(bjdongCd)
                .platGbCd(platGbCd)
                .bun(bun)
                .ji(ji)
                .build();

        return ResponseEntity.ok(ApiResponse.success(
                buildingLedgerClient.extractDongs(
                        buildingLedgerClient.fetchTitleItems(address))));
    }
}
