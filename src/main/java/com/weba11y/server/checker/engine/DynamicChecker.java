package com.weba11y.server.checker.engine;

import com.microsoft.playwright.Page;
import com.weba11y.server.api.dto.accessibilityViolation.AccessibilityViolationDto;

import java.util.List;

// 동적 검사기만 태깅할 마커 인터페이스
public interface DynamicChecker extends AccessibilityChecker {
    List<AccessibilityViolationDto> checkDynamic(Page page, Long summaryId);
}
