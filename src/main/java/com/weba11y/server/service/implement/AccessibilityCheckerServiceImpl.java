package com.weba11y.server.service.implement;

import com.weba11y.server.checker.SseEventSender;
import com.weba11y.server.checker.StaticContentAccessibilityChecker;
import com.weba11y.server.domain.InspectionSummary;
import com.weba11y.server.domain.InspectionUrl;
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

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AccessibilityCheckerServiceImpl implements AccessibilityCheckerService {
    private final PageLoaderService pageLoaderService;
    private final StaticContentAccessibilityChecker staticContentAccessibilityChecker;
    private final InspectionUrlRepository urlRepository;
    private final InspectionSummaryRepository summaryRepository;
    private final SseEventSender sseEventSender;

    @Override
    public SseEmitter runChecks(InspectionUrlDto inspectionUrl, Long memberId) {
        SseEmitter emitter = new SseEmitter(6000L); // 1분 타임아웃
        emitter.onCompletion(() -> log.info("SSE completed for client."));
        emitter.onTimeout(() -> log.warn("SSE timed out for client."));
        emitter.onError(e -> log.error("SSE error for client: ", e));

        sseEventSender.send(emitter, "connect", "Connection established. Starting accessibility check...");

        runChecksAsync(inspectionUrl, emitter);

        return emitter;
    }

    @Async("taskExecutor")
    public void runChecksAsync(InspectionUrlDto inspectionUrl, SseEmitter emitter) {
        try {
            String content = pageLoaderService.getLoadedPage(inspectionUrl.getUrl()).content();
            InspectionSummary inspectionSummary = createInspectionSummary(retrieveInspectionUrl(inspectionUrl.getId()));
            Document document = Jsoup.parse(content);

            staticContentAccessibilityChecker.performCheck(document, emitter, inspectionSummary)
                    .exceptionally(ex -> {
                        handleAsyncException(emitter, ex);
                        return null;
                    });

        } catch (Exception e) {
            handleAsyncException(emitter, e);
        }
    }

    private void handleAsyncException(SseEmitter emitter, Throwable ex) {
        log.error("Error during accessibility check process", ex);
        sseEventSender.sendErrorEvent(emitter, "Failed to start check: " + ex.getMessage());
        emitter.completeWithError(ex);
    }

    private InspectionUrl retrieveInspectionUrl(Long inspectionUrlId) {
        return urlRepository.findById(inspectionUrlId).orElseThrow(
                () -> new NoSuchElementException("InspectionUrl Not Found")
        );
    }

    @Transactional
    public InspectionSummary createInspectionSummary(InspectionUrl inspectionUrl) {
        log.info("[AccessibilityCheckerServiceImpl] Creating Inspection Summary...");
        try {
            return summaryRepository.save(InspectionSummary.builder()
                    .inspectionUrl(inspectionUrl)
                    .build());
        } catch (Exception e) {
            log.error("Failed to create Inspection Summary: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create Inspection Summary", e);
        }
    }
}
