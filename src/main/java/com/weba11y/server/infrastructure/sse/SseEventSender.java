package com.weba11y.server.infrastructure.sse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weba11y.server.api.dto.accessibilityViolation.AccessibilityViolationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class SseEventSender {

    private final ObjectMapper objectMapper;

    public void send(SseEmitter emitter, String eventName, Object data) {
        try {
            String jsonData = objectMapper.writeValueAsString(data);
            emitter.send(SseEmitter.event().name(eventName).data(jsonData));
        } catch (JsonProcessingException e) {
            log.error("Failed to convert {} data to JSON: {}", eventName, e.getMessage(), e);
        } catch (IOException e) {
            log.warn("Failed to send SSE {} event: {}. Emitter might be closed.", eventName, e.getMessage());
        } catch (Exception e) {
            log.error("An unexpected error occurred while sending {} event: {}", eventName, e.getMessage(), e);
        }
    }

    public void sendViolationEvent(SseEmitter emitter, AccessibilityViolationDto violation) {
        this.send(emitter, "violation", violation);
    }

    public void sendErrorEvent(SseEmitter emitter, String errorMessage) {
        this.send(emitter, "error", errorMessage);
    }
}
