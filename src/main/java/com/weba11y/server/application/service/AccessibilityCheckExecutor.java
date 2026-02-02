package com.weba11y.server.application.service;

import com.microsoft.playwright.Page;
import com.weba11y.server.checker.engine.DynamicContentAccessibilityChecker;
import com.weba11y.server.infrastructure.sse.SseEventSender;
import com.weba11y.server.checker.engine.StaticContentAccessibilityChecker;
import com.weba11y.server.domain.inspection.summary.InspectionSummary;
import com.weba11y.server.domain.enums.InspectionStatus;
import com.weba11y.server.api.dto.inspectionUrl.InspectionUrlDto;
import com.weba11y.server.application.service.InspectionPersistenceService;
import com.weba11y.server.infrastructure.playwright.PageLoaderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccessibilityCheckExecutor {
    private final PageLoaderService pageLoaderService;
    private final StaticContentAccessibilityChecker staticContentAccessibilityChecker;
    private final DynamicContentAccessibilityChecker dynamicContentAccessibilityChecker;
    private final InspectionPersistenceService inspectionPersistenceService;
    private final SseEventSender sseEventSender;

    @Async("taskExecutor")
    public void runChecksAsync(InspectionUrlDto inspectionUrl, SseEmitter emitter) {
        InspectionSummary summary = inspectionPersistenceService.createAndPrepareInspectionSummary(inspectionUrl.getId());
        final AtomicBoolean isErrorHandled = new AtomicBoolean(false);

        try {
            Page page = pageLoaderService.getLoadedPage(inspectionUrl.getUrl());
            String content = page.content();
            Document document = Jsoup.parse(content);

            CompletableFuture<Void> staticCheckFuture = staticContentAccessibilityChecker.performCheck(document, emitter, summary);
            CompletableFuture<Void> dynamicCheckFuture = dynamicContentAccessibilityChecker.performCheck(page, emitter, summary);

            CompletableFuture.allOf(staticCheckFuture, dynamicCheckFuture)
                    .thenRun(() -> {
                        inspectionPersistenceService.updateInspectionStatus(summary.getId(), InspectionStatus.COMPLETED);
                        sseEventSender.send(emitter, "complete", "All accessibility checks completed.");
                        emitter.complete();
                    })
                    .exceptionally(ex -> {
                        if (isErrorHandled.compareAndSet(false, true)) {
                            handleAsyncException(emitter, summary.getId(), ex);
                        }
                        return null;
                    });

        } catch (Exception e) {
            if (isErrorHandled.compareAndSet(false, true)) {
                handleAsyncException(emitter, summary.getId(), e);
            }
        }
    }

    private void handleAsyncException(SseEmitter emitter, Long summaryId, Throwable ex) {
        log.error("Error during accessibility check process for summaryId: {}", summaryId, ex);
        inspectionPersistenceService.updateInspectionStatus(summaryId, InspectionStatus.FAILED);
        sseEventSender.sendErrorEvent(emitter, "Failed to perform accessibility check: " + ex.getMessage());
        emitter.completeWithError(ex);
    }
}
