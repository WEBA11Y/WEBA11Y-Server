package com.weba11y.server.service;


import com.weba11y.server.dto.accessibilityViolation.AccessibilityViolationDto;
import com.weba11y.server.dto.inspectionUrl.InspectionUrlDto;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

public interface AccessibilityCheckerService {

    List<AccessibilityViolationDto> runChecks(InspectionUrlDto inspectionUrl, SseEmitter sseEmitter);
}
