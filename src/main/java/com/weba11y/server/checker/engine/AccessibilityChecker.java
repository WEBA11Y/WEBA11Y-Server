package com.weba11y.server.checker.engine;

import com.weba11y.server.api.dto.accessibilityViolation.AccessibilityViolationDto;
import org.jsoup.nodes.Document;

import java.util.List;

public interface AccessibilityChecker {
    List<AccessibilityViolationDto> check(Document doc, Long inspectionSummaryId);
}
