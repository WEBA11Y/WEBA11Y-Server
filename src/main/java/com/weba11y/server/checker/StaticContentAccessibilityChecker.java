package com.weba11y.server.checker;

import com.weba11y.server.domain.InspectionSummary;
import org.jsoup.nodes.Document;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.CompletableFuture;

public interface StaticContentAccessibilityChecker {
    CompletableFuture<Void> performCheck(Document document, SseEmitter emitter, InspectionSummary inspectionSummary);
}