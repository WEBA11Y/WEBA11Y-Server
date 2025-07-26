package com.weba11y.server.controller;


import com.weba11y.server.annotation.CurrentMemberId;
import com.weba11y.server.service.InspectionSummaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "검사 결과 요약 API", description = "웹 접근성 검사 결과의 요약 정보를 관리하는 API 입니다.")
@RequestMapping("/api/v1")
@RestController
@RequiredArgsConstructor
public class InspectionSummaryController {
    private final InspectionSummaryService inspectionSummaryService;

    @GetMapping("/inspection-summaries/metadata")
    @Operation(summary = "검사 결과 요약 Metadata 리스트")
    private ResponseEntity<List> getInspectionSummariesMataData(
            @CurrentMemberId Long memberId,
            @RequestParam("urlId") Long urlId) {
        return ResponseEntity
                .ok()
                .body(inspectionSummaryService.retrieveSummariesMetadataByUrlAndMember(urlId, memberId));
    }
}
