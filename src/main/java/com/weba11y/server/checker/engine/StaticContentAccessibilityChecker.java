package com.weba11y.server.checker.engine;

import com.weba11y.server.domain.inspection.summary.InspectionSummary;
import org.jsoup.nodes.Document;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.CompletableFuture;

public interface StaticContentAccessibilityChecker {
    CompletableFuture<Void> performCheck(Document document, SseEmitter emitter, InspectionSummary inspectionSummary);
}