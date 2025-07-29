package com.weba11y.server.checker.implement;

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Page;
import com.weba11y.server.checker.AbstractAccessibilityChecker;
import com.weba11y.server.checker.DynamicChecker;
import com.weba11y.server.domain.enums.InspectionItems;
import com.weba11y.server.dto.accessibilityViolation.AccessibilityViolationDto;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class MotionActivationCheck extends AbstractAccessibilityChecker implements DynamicChecker {

    @Override
    protected InspectionItems getItem() {
        return InspectionItems.MOTION_ACTIVATION;
    }

    @Override
    public List<AccessibilityViolationDto> checkDynamic(Page page, Long summaryId) {
        List<AccessibilityViolationDto> violations = new ArrayList<>();

        // 기기 센서 기반 기능 (예: device-motion 이벤트) 감지
        List<ElementHandle> motionElements = page.querySelectorAll("[data-motion], [data-tilt-control]");
        for (ElementHandle el : motionElements) {
            boolean hasAltControl = Boolean.TRUE.equals(page.evaluate("el => el.hasAttribute('data-alt-control')", el));
            if (!hasAltControl) {
                violations.add(AccessibilityViolationDto.createViolationDto(el, getItem(), summaryId));
                log.debug("[MotionActivationCheck] 대체 조작 수단 없음: element={}", el);
            }
        }
        return violations;
    }

    @Override
    protected boolean isViolation(Element element) {
        return false;
    }
}

