package com.weba11y.server.service;

import com.weba11y.server.dto.inspectionUrl.InspectionUrlDto;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface AccessibilityCheckerService {

    SseEmitter runChecks(InspectionUrlDto inspectionUrl, Long memberId);
}
