package com.weba11y.server.service.implement;

import com.microsoft.playwright.Page;
import com.weba11y.server.checker.DynamicContentAccessibilityChecker;
import com.weba11y.server.checker.SseEventSender;
import com.weba11y.server.checker.StaticContentAccessibilityChecker;
import com.weba11y.server.domain.InspectionSummary;
import com.weba11y.server.domain.InspectionUrl;
import com.weba11y.server.domain.enums.InspectionStatus;
import com.weba11y.server.dto.inspectionUrl.InspectionUrlDto;
import com.weba11y.server.repository.InspectionSummaryRepository;
import com.weba11y.server.repository.InspectionUrlRepository;
import com.weba11y.server.service.AccessibilityCheckerService;
import com.weba11y.server.service.PageLoaderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AccessibilityCheckerServiceImpl implements AccessibilityCheckerService {
    private final PageLoaderService pageLoaderService;
    private final StaticContentAccessibilityChecker staticContentAccessibilityChecker;
    private final DynamicContentAccessibilityChecker dynamicContentAccessibilityChecker;
    private final InspectionUrlRepository urlRepository;
    private final InspectionSummaryRepository summaryRepository;
    private final SseEventSender sseEventSender;

    @Override
    public SseEmitter runChecks(InspectionUrlDto inspectionUrl, Long memberId) {
        SseEmitter emitter = new SseEmitter(60000L); // 1분 타임아웃
        emitter.onCompletion(() -> log.info("SSE completed for client."));
        emitter.onTimeout(() -> {
            log.warn("SSE timed out for client.");
            sseEventSender.sendErrorEvent(emitter, "Accessibility check timed out.");
            emitter.complete();
        });
        emitter.onError(e -> log.error("SSE error for client: ", e));

        sseEventSender.send(emitter, "connect", "Connection established. Starting accessibility check...");

        runChecksAsync(inspectionUrl, emitter);

        return emitter;
    }

    @Async("taskExecutor")
    public void runChecksAsync(InspectionUrlDto inspectionUrl, SseEmitter emitter) {
        InspectionSummary summary = createAndPrepareInspectionSummary(inspectionUrl.getId());
        final AtomicBoolean isErrorHandled = new AtomicBoolean(false);

        try {
            Page page = pageLoaderService.getLoadedPage(inspectionUrl.getUrl());
            String content = page.content();
            Document document = Jsoup.parse(content);

            CompletableFuture<Void> staticCheckFuture = staticContentAccessibilityChecker.performCheck(document, emitter, summary);
            CompletableFuture<Void> dynamicCheckFuture = dynamicContentAccessibilityChecker.performCheck(page, emitter, summary);

            CompletableFuture.allOf(staticCheckFuture, dynamicCheckFuture)
                    .thenRun(() -> {
                        updateInspectionStatus(summary.getId(), InspectionStatus.COMPLETED);
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
        updateInspectionStatus(summaryId, InspectionStatus.FAILED);
        sseEventSender.sendErrorEvent(emitter, "Failed to perform accessibility check: " + ex.getMessage());
        emitter.completeWithError(ex);
    }

    private InspectionUrl retrieveInspectionUrl(Long inspectionUrlId) {
        return urlRepository.findById(inspectionUrlId).orElseThrow(
                () -> new NoSuchElementException("InspectionUrl Not Found: " + inspectionUrlId)
        );
    }

    @Transactional
    public InspectionSummary createAndPrepareInspectionSummary(Long inspectionUrlId) {
        log.info("[AccessibilityCheckerServiceImpl] Creating Inspection Summary for URL id: {}", inspectionUrlId);
        InspectionUrl inspectionUrl = retrieveInspectionUrl(inspectionUrlId);
        InspectionSummary summary = InspectionSummary.builder()
                .inspectionUrl(inspectionUrl)
                .status(InspectionStatus.IN_PROGRESS)
                .build();
        return summaryRepository.save(summary);
    }

    @Transactional
    public void updateInspectionStatus(Long summaryId, InspectionStatus status) {
        try {
            summaryRepository.findById(summaryId).ifPresent(summary -> {
                summary.updateStatus(status);
                summaryRepository.save(summary);
                log.info("InspectionSummary {} status updated to {}", summaryId, status);
            });
        } catch (Exception e) {
            log.error("Failed to update InspectionSummary status for id: {}", summaryId, e);
        }
    }
}
