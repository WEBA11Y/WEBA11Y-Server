package com.weba11y.server.controller;

import com.weba11y.server.dto.accessibilityViolation.AccessibilityViolationDto;
import com.weba11y.server.service.AccessibilityViolationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "웹 접근성 검사 위반 내역 API", description = "웹 접근성 검사 위반 내역을 관리하는 API 입니다.")
@RequestMapping("/api/v1")
public class AccessibilityViolationController {

    private final AccessibilityViolationService service;

    @GetMapping("/violations")
    @Operation(summary = "검사 위반 내역 리스트")
    public ResponseEntity<AccessibilityViolationDto.AccessibilityViolationsResponse> getAccessibilityViolations(
            @Parameter(description = "검사 요약 ID", required = true) @RequestParam("is_id") Long inspectionSummaryId,
            @RequestParam(name = "type", defaultValue = "importance") String type,
            @RequestParam(name = "val", defaultValue = "CRITICAL") String value,
            @RequestParam(name = "page", defaultValue = "1") int page
    ) {
        AccessibilityViolationDto.AccessibilityViolationsResponse violationsResponse =
                type.toUpperCase().equals("IMPORTANCE")
                        ? service.getViolationsByImportance(page, inspectionSummaryId, value)
                        : service.getViolationsByAssessmentLevel(page, inspectionSummaryId, value);
        return ResponseEntity.ok(violationsResponse);
    }
}
