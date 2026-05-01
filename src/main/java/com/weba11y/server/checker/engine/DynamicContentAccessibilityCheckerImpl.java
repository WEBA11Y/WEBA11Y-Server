package com.weba11y.server.checker.engine;

import com.microsoft.playwright.Page;
import com.weba11y.server.infrastructure.sse.SseEventSender;
import com.weba11y.server.api.dto.accessibilityViolation.AccessibilityViolationDto;
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

    @Override
    @Async("taskExecutor")
    public CompletableFuture<List<AccessibilityViolationDto>> performCheck(Page page, SseEmitter emitter, Long summaryId) {
        log.info("[DynamicCheckerImpl] Starting dynamic accessibility checks for inspection: {}", summaryId);

        try {
            List<AccessibilityViolationDto> totalViolations = new java.util.ArrayList<>();

            for (DynamicChecker checker : dynamicCheckers) {
                if (page.isClosed()) {
                    log.warn("Page was closed, stopping dynamic checks for inspection: {}", summaryId);
                    break;
                }
                try {
                    List<AccessibilityViolationDto> violations = checker.checkDynamic(page, summaryId);
                    violations.forEach(v -> sseEventSender.sendViolationEvent(emitter, v));
                    totalViolations.addAll(violations);
                } catch (Exception e) {
                    log.error("[DynamicCheckerImpl] Error in checker '{}' for inspection: {}. Skipping checker.",
                            checker.getClass().getSimpleName(), summaryId, e);
                    sseEventSender.sendErrorEvent(emitter, "Error in checker " + checker.getClass().getSimpleName() + ": " + e.getMessage());
                }
            }

            log.info("[DynamicCheckerImpl] Finished all dynamic checks for inspection: {}. Total violations: {}",
                    summaryId, totalViolations.size());

            return CompletableFuture.completedFuture(totalViolations);

        } catch (Exception e) {
            log.error("[DynamicCheckerImpl] Unrecoverable error during dynamic check process for inspection: {}", summaryId, e);
            return CompletableFuture.failedFuture(e);
        }
    }

}
