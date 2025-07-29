package com.weba11y.server.checker;

import com.microsoft.playwright.Page;
import com.weba11y.server.domain.InspectionSummary;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.CompletableFuture;

public interface DynamicContentAccessibilityChecker {
    CompletableFuture<Void> performCheck(Page page, SseEmitter emitter, InspectionSummary summary);
}
