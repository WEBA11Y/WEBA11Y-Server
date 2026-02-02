package com.weba11y.server.checker.engine;

import com.weba11y.server.checker.engine.AccessibilityChecker;
import com.weba11y.server.infrastructure.sse.SseEventSender;
import com.weba11y.server.checker.engine.StaticContentAccessibilityChecker;
import com.weba11y.server.domain.inspection.summary.InspectionSummary;
import com.weba11y.server.api.dto.accessibilityViolation.AccessibilityViolationDto;
import com.weba11y.server.application.service.InspectionPersistenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StaticContentAccessibilityCheckerImpl implements StaticContentAccessibilityChecker {

    private final List<AccessibilityChecker> checkers;
    private final SseEventSender sseEventSender;
    private final InspectionPersistenceService inspectionPersistenceService;

    @Override
    @Async("taskExecutor")
    public CompletableFuture<Void> performCheck(Document document, SseEmitter emitter, InspectionSummary inspectionSummary) {
        log.info("[StaticCheckerImpl] Starting static content accessibility check for inspection: {}", inspectionSummary.getId());

        try {
            List<CompletableFuture<List<AccessibilityViolationDto>>> futures = checkers.stream()
                    .map(checker -> CompletableFuture.supplyAsync(() -> {
                        List<AccessibilityViolationDto> violations = checker.check(document, inspectionSummary.getId());
                        violations.forEach(violation -> sseEventSender.sendViolationEvent(emitter, violation));
                        return violations;
                    }))
                    .collect(Collectors.toList());

            return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .thenAccept(v -> {
                        List<AccessibilityViolationDto> totalViolations = futures.stream()
                                .flatMap(future -> future.join().stream())
                                .collect(Collectors.toList());

                        inspectionPersistenceService.updateInspectionSummary(inspectionSummary, totalViolations);

                        log.info("[StaticCheckerImpl] Finished all checks for inspection: {}. Total violations: {}", inspectionSummary.getId(), totalViolations.size());
                    });

        } catch (Exception e) {
            handleException(emitter, e, inspectionSummary.getId());
            return CompletableFuture.failedFuture(e);
        }
    }

    private void handleException(SseEmitter emitter, Throwable ex, Long inspectionId) {
        log.error("[StaticCheckerImpl] Error during accessibility check for inspection: {}", inspectionId, ex);
        sseEventSender.sendErrorEvent(emitter, "An error occurred during the check: " + ex.getMessage());
        emitter.completeWithError(ex);
    }
}
