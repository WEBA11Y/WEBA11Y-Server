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
public class UserControlCheck extends AbstractAccessibilityChecker implements DynamicChecker {

    @Override
    protected InspectionItems getItem() {
        return InspectionItems.USER_CONTROL;
    }

    @Override
    public List<AccessibilityViolationDto> checkDynamic(Page page, Long summaryId) {
        List<AccessibilityViolationDto> violations = new ArrayList<>();

        // 자동 슬라이드(캐러셀) 또는 marquee, video autoplay 탐지
        List<ElementHandle> elements = page.querySelectorAll("marquee, video[autoplay], [data-auto-slide]");
        for (ElementHandle el : elements) {
            boolean hasControls = Boolean.TRUE.equals(page.evaluate("el => el.hasAttribute('controls')", el));
            if (!hasControls) {
                violations.add(AccessibilityViolationDto.createViolationDto(el, getItem(), summaryId));
                log.debug("[UserControlCheck] 자동 변경 콘텐츠 제어 불가: element={}", el);
            }
        }
        return violations;
    }

    @Override
    protected boolean isViolation(Element element) {
        return false;
    }
}
