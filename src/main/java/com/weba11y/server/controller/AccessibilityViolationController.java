package com.weba11y.server.controller;


import com.weba11y.server.domain.enums.AssessmentLevel;
import com.weba11y.server.domain.enums.Importance;
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

    @GetMapping("/violations/by-importance") // 중요도 필터링을 명확히
    @Operation(summary = "중요도별 검사 위반 내역 리스트", description = "특정 검사 요약에 대해 선택한 중요도 기준으로 검사 위반 내역을 제공합니다.")
    public ResponseEntity<AccessibilityViolationDto.AccessibilityViolationsResponse> getAccessibilityViolations(
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "검사 요약 ID", required = true) @RequestParam("summaryId") Long inspectionSummaryId,
            @RequestParam(name = "importance", defaultValue = "CRITICAL") Importance importance) {
        return ResponseEntity.ok().body(service.getViolationsByImportance(page, inspectionSummaryId, importance));
    }

    @GetMapping("/violations/by-assessment-level") // 평가 레벨 필터링을 명확히
    @Operation(summary = "평가 레벨별 검사 위반 내역 리스트", description = "특정 검사 요약에 대해 선택한 평가 레벨을 기준으로 검사 위반 내역을 제공합니다.")
    public ResponseEntity<AccessibilityViolationDto.AccessibilityViolationsResponse> getAccessibilityViolations(
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "검사 요약 ID", required = true) @RequestParam("summaryId") Long inspectionSummaryId,
            @RequestParam(name = "level", defaultValue = "AAA") AssessmentLevel assessmentLevel) {
        return ResponseEntity.ok().body(service.getViolationsByAssessmentLevel(page, inspectionSummaryId, assessmentLevel));
    }

}
