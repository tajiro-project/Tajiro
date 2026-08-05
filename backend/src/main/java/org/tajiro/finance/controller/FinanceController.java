/**
 * KB 금융상품 목록·상세·조건 매칭 API (12-2, 24).
 */
package org.tajiro.finance.controller;


import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.tajiro.finance.dto.FinanceDTO;
import org.tajiro.finance.service.FinanceService;
import org.tajiro.finance.service.FinanceServiceImpl;

import java.util.List;

@RestController
@RequestMapping("/api/financial-products")
@RequiredArgsConstructor
@Api(tags = "KB금융 상품 관리")
public class FinanceController {
    final FinanceServiceImpl service;

    @GetMapping("/matches")
    public ResponseEntity<List<FinanceDTO>> getList(@RequestParam(required = false) String keyword){

        return ResponseEntity.ok(service.getList(keyword));
    }

    @GetMapping("")
    public ResponseEntity<FinanceDTO> get(@RequestParam(required = true) long id){

        return ResponseEntity.ok(service.get(id));
    }



}