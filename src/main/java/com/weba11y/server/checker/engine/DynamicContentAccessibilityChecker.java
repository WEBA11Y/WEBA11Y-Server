package com.weba11y.server.checker.engine;

import com.microsoft.playwright.Page;
import com.weba11y.server.api.dto.accessibilityViolation.AccessibilityViolationDto;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface DynamicContentAccessibilityChecker {
    CompletableFuture<List<AccessibilityViolationDto>> performCheck(Page page, SseEmitter emitter, Long summaryId);
}
