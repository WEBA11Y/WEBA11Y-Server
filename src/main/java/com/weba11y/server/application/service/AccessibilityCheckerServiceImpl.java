package com.weba11y.server.application.service;

import com.weba11y.server.infrastructure.sse.SseEventSender;
import com.weba11y.server.api.dto.inspectionUrl.InspectionUrlDto;
import com.weba11y.server.application.service.AccessibilityCheckerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccessibilityCheckerServiceImpl implements AccessibilityCheckerService {
    private final AccessibilityCheckExecutor accessibilityCheckExecutor;
    private final SseEventSender sseEventSender;

    @Override
    public SseEmitter runChecks(InspectionUrlDto inspectionUrl, Long memberId) {
        SseEmitter emitter = new SseEmitter(300000L); // 5분 타임아웃
        emitter.onCompletion(() -> log.info("SSE completed for client."));
        emitter.onTimeout(() -> {
            log.warn("SSE timed out for client.");
            sseEventSender.sendErrorEvent(emitter, "Accessibility check timed out.");
            emitter.complete();
        });
        emitter.onError(e -> log.error("SSE error for client: ", e));
        sseEventSender.send(emitter, "connect", "Connection established. Starting accessibility check...");
        accessibilityCheckExecutor.runChecksAsync(inspectionUrl, emitter);
        return emitter;
    }
}
