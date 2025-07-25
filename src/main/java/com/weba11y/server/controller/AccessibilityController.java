package com.weba11y.server.controller;

import com.weba11y.server.annotation.CurrentMemberId;
import com.weba11y.server.domain.InspectionSummary;
import com.weba11y.server.dto.accessibilityViolation.AccessibilityViolationDto;
import com.weba11y.server.dto.inspectionUrl.InspectionUrlDto;
import com.weba11y.server.service.AccessibilityCheckerService;
import com.weba11y.server.service.InspectionSummaryService;
import com.weba11y.server.service.InspectionUrlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "웹 접근성 검사 API", description = "웹 접근성 검사 관련 비동기 API를 제공합니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class AccessibilityController {

    private final AccessibilityCheckerService checkerService;
    private final InspectionUrlService inspectionUrlService;
    private final InspectionSummaryService inspectionSummaryService;
    /*@GetMapping(value = "/accessibility", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "웹 접근성 검사", description = "비동기적으로 웹 접근성 검사를 수행하고 단방향 실시간 데이터 스트리밍을 제공합니다.")
    public List<AccessibilityViolationDto> checkAccessibility(@RequestParam Long urlId, @CurrentMemberId Long memberId) {
        InspectionUrlDto inspectionUrl = inspectionUrlService.retrieveUrl(urlId, memberId);
        return checkerService.runChecks(inspectionUrl);
    }*/
}
