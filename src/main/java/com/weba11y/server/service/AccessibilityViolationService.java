package com.weba11y.server.service;

import com.weba11y.server.domain.enums.AssessmentLevel;
import com.weba11y.server.domain.enums.Importance;
import com.weba11y.server.dto.accessibilityViolation.AccessibilityViolationDto;

import java.util.List;

public interface AccessibilityViolationService {

    // 해당 검사 결과 모두 가져오기
    List<AccessibilityViolationDto> getViolationsByInspectionSummaryId(Long inspectionSummaryId);

    // 중요도 별로 가져오기 (페이징)
    AccessibilityViolationDto.AccessibilityViolationsResponse getViolationsByImportance(
            int page,
            Long inspectionSummaryId,
            Importance importance);

    // 평가 레벨 별로 가져오기 (페이징)
    AccessibilityViolationDto.AccessibilityViolationsResponse getViolationsByAssessmentLevel(
            int page,
            Long inspectionSummaryId,
            AssessmentLevel assessmentLevel);

    // 평가 레벨 및 중요도 기준 TOP5 찾아오기
    List<AccessibilityViolationDto> getTop5ViolationsBySummaryId(Long inspectionSummaryId);

}
