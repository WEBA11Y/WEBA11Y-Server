package com.weba11y.server.checker.implement;

import com.microsoft.playwright.Page;
import com.weba11y.server.checker.AccessibilityChecker;
import com.weba11y.server.checker.DynamicChecker;
import com.weba11y.server.checker.DynamicContentAccessibilityChecker;
import com.weba11y.server.checker.SseEventSender;
import com.weba11y.server.domain.InspectionSummary;
import com.weba11y.server.dto.accessibilityViolation.AccessibilityViolationDto;
import com.weba11y.server.repository.InspectionSummaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class DynamicContentAccessibilityCheckerImpl implements DynamicContentAccessibilityChecker {

    private final List<DynamicChecker> dynamicCheckers;  // 동적 항목 전용 검사기
    private final SseEventSender sseEventSender;
    private final InspectionSummaryRepository inspectionSummaryRepository;

    @Override
    @Async("taskExecutor")
    @Transactional
    public CompletableFuture<Void> performCheck(Page page, SseEmitter emitter, InspectionSummary summary) {
        log.info("[DynamicCheckerImpl] Starting dynamic accessibility checks for inspection: {}", summary.getId());

        // CompletableFuture.runAsync를 사용하여 전체 로직을 비동기 작업으로 감쌉니다.
        return CompletableFuture.runAsync(() -> {
            try {
                List<AccessibilityViolationDto> totalViolations = new java.util.ArrayList<>();

                // 각 DynamicChecker를 순차적으로 실행합니다.
                for (DynamicChecker checker : dynamicCheckers) {
                    // 매번 검사 전에 페이지가 닫혔는지 확인합니다.
                    if (page.isClosed()) {
                        log.warn("Page was closed, stopping dynamic checks for inspection: {}", summary.getId());
                        break; // 페이지가 닫혔으면 더 이상 검사를 진행하지 않습니다.
                    }

                    try {
                        // 개별 검사기 실행
                        List<AccessibilityViolationDto> violations = checker.checkDynamic(page, summary.getId());
                        // 검사 결과(위반 사항)를 SSE로 즉시 전송합니다.
                        violations.forEach(v -> sseEventSender.sendViolationEvent(emitter, v));
                        // 전체 위반 사항 리스트에 추가합니다.
                        totalViolations.addAll(violations);
                    } catch (Exception e) {
                        // 특정 검사기에서 오류가 발생하더라도 전체 검사가 중단되지 않도록 처리합니다.
                        log.error("[DynamicCheckerImpl] Error in checker '{}' for inspection: {}. Skipping checker.",
                                checker.getClass().getSimpleName(), summary.getId(), e);
                        // 클라이언트에게 어떤 검사기에서 오류가 발생했는지 알려줄 수 있습니다.
                        sseEventSender.sendErrorEvent(emitter, "Error in checker " + checker.getClass().getSimpleName() + ": " + e.getMessage());
                    }
                }

                // 모든 검사가 완료된 후, 최종적으로 수집된 위반 사항으로 Summary를 업데이트합니다.
                updateInspectionSummary(summary, totalViolations);

                log.info("[DynamicCheckerImpl] Finished all dynamic checks for inspection: {}. Total violations: {}",
                        summary.getId(), totalViolations.size());

            } catch (Exception e) {
                // 루프 외부의 로직(예: updateInspectionSummary)에서 발생할 수 있는 예외를 처리합니다.
                log.error("[DynamicCheckerImpl] Unrecoverable error during dynamic check process for inspection: {}", summary.getId(), e);
                // 이 예외는 CompletableFuture를 실패 상태로 만듭니다.
                throw new java.util.concurrent.CompletionException(e);
            }
        });
    }

    private void updateInspectionSummary(InspectionSummary summary, List<AccessibilityViolationDto> violations) {
        violations.forEach(dto -> summary.addViolation(dto.toEntity(summary)));
        summary.recalculateViolations();
        inspectionSummaryRepository.save(summary);
    }

    private void handleException(SseEmitter emitter, Throwable ex, Long inspectionId) {
        log.error("[DynamicCheckerImpl] Error during dynamic accessibility check: {}", inspectionId, ex);
        sseEventSender.sendErrorEvent(emitter, "Dynamic check failed: " + ex.getMessage());
        emitter.completeWithError(ex);
    }
}
