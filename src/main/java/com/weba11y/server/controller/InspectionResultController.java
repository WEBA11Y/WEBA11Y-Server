package com.weba11y.server.controller;


import com.weba11y.server.service.InspectionResultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "검사 결과 내역 API", description = "검사 결과 내역을 관리하는 API 입니다.")
@RequestMapping("/api/v1")
public class InspectionResultController {

    private final InspectionResultService service;

    @GetMapping("/inspection-date/{urlId}")
    @Operation(summary = "검사 날짜 리스트", description = "검사한 날짜를 담은 List 입니다.")
    public ResponseEntity<List> getInspectionDate(@PathVariable("urlId") Long urlId) {
        return ResponseEntity.ok().body(service.retrieveInspectionResultDateByUrlId(urlId));
    }

    @GetMapping("/inspection-results/{urlId}")
    @Operation(summary = "날짜별 검사 내역 리스트", description = "선택한 날짜의 검사 결과 내역을 제공합니다.")
    public ResponseEntity<List> getInspectionResults(@PathVariable("urlId") Long urlId,
                                                     @RequestParam("date") LocalDate date) {
        return ResponseEntity.ok().body(service.retrieveResultsByUrlIdAndDate(urlId, date));
    }
}
