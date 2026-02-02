package com.weba11y.server.checker.engine;

import com.microsoft.playwright.Page;
import com.weba11y.server.checker.engine.DynamicChecker;
import com.weba11y.server.checker.engine.DynamicContentAccessibilityChecker;
import com.weba11y.server.infrastructure.sse.SseEventSender;
import com.weba11y.server.domain.inspection.summary.InspectionSummary;
import com.weba11y.server.api.dto.accessibilityViolation.AccessibilityViolationDto;
import com.weba11y.server.application.service.InspectionPersistenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class DynamicContentAccessibilityCheckerImpl implements DynamicContentAccessibilityChecker {

    private final List<DynamicChecker> dynamicCheckers;
    private final SseEventSender sseEventSender;
    private final InspectionPersistenceService inspectionPersistenceService;

    @Override
    @Async("taskExecutor")
    public CompletableFuture<Void> performCheck(Page page, SseEmitter emitter, InspectionSummary summary) {
        log.info("[DynamicCheckerImpl] Starting dynamic accessibility checks for inspection: {}", summary.getId());
        return CompletableFuture.runAsync(() -> {
            try {
                List<AccessibilityViolationDto> totalViolations = new java.util.ArrayList<>();
                for (DynamicChecker checker : dynamicCheckers) {
                    if (page.isClosed()) {
                        log.warn("Page was closed, stopping dynamic checks for inspection: {}", summary.getId());
                        break;
                    }
                    try {
                        List<AccessibilityViolationDto> violations = checker.checkDynamic(page, summary.getId());
                        violations.forEach(v -> sseEventSender.sendViolationEvent(emitter, v));
                        totalViolations.addAll(violations);
                    } catch (Exception e) {
                        log.error("[DynamicCheckerImpl] Error in checker '{}' for inspection: {}. Skipping checker.",
                                checker.getClass().getSimpleName(), summary.getId(), e);
                        sseEventSender.sendErrorEvent(emitter, "Error in checker " + checker.getClass().getSimpleName() + ": " + e.getMessage());
                    }
                }

                inspectionPersistenceService.updateInspectionSummary(summary, totalViolations);

                log.info("[DynamicCheckerImpl] Finished all dynamic checks for inspection: {}. Total violations: {}",
                        summary.getId(), totalViolations.size());

            } catch (Exception e) {
                log.error("[DynamicCheckerImpl] Unrecoverable error during dynamic check process for inspection: {}", summary.getId(), e);
                throw new java.util.concurrent.CompletionException(e);
            }
        });
    }

    private void handleException(SseEmitter emitter, Throwable ex, Long inspectionId) {
        log.error("[DynamicCheckerImpl] Error during dynamic accessibility check: {}", inspectionId, ex);
        sseEventSender.sendErrorEvent(emitter, "Dynamic check failed: " + ex.getMessage());
        emitter.completeWithError(ex);
    }
}
