package com.weba11y.server.checker.engine;

import com.weba11y.server.api.dto.accessibilityViolation.AccessibilityViolationDto;
import org.jsoup.nodes.Document;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface StaticContentAccessibilityChecker {
    CompletableFuture<List<AccessibilityViolationDto>> performCheck(Document document, SseEmitter emitter, Long summaryId);
}