package com.weba11y.server.checker;

import com.weba11y.server.dto.accessibilityViolation.AccessibilityViolationDto;
import org.jsoup.nodes.Document;

import java.util.List;

public interface AccessibilityChecker {
    List<AccessibilityViolationDto> check(List<AccessibilityViolationDto> violations, Document doc);

}
