package com.weba11y.server.checker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weba11y.server.dto.accessibilityViolation.AccessibilityViolationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@Slf4j
@Component
public class SseEventSender {

    private final ObjectMapper objectMapper;

    public SseEventSender(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void sendViolationEvent(SseEmitter emitter, AccessibilityViolationDto violation) {
        try {
            String jsonDto = objectMapper.writeValueAsString(violation);
            emitter.send(SseEmitter.event().name("violation").data(jsonDto));
        } catch (JsonProcessingException e) {
            log.error("Failed to convert AccessibilityViolationDto to JSON: {}", e.getMessage(), e);
        } catch (IOException e) {
            log.warn("Failed to send SSE violation event: {}. Emitter might be closed.", e.getMessage());
            // SseEmitter가 이미 종료되었을 수 있음. 더 이상 보낼 수 없음.
        } catch (Exception e) { // 기타 예상치 못한 예외 처리
            log.error("An unexpected error occurred while sending violation event: {}", e.getMessage(), e);
        }
    }

    public void sendErrorEvent(SseEmitter emitter, String errorMessage) {
        try {
            emitter.send(SseEmitter.event().name("error").data(errorMessage));
        } catch (IOException e) {
            log.warn("Failed to send SSE error event: {}. Emitter might be closed.", e.getMessage());
        } catch (Exception e) { // 기타 예상치 못한 예외 처리
            log.error("An unexpected error occurred while sending error event: {}", e.getMessage(), e);
        }
    }
}
