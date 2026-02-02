package com.weba11y.server.checker.rule.operable;

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Page;
import com.weba11y.server.checker.engine.AbstractAccessibilityChecker;
import com.weba11y.server.checker.engine.DynamicChecker;
import com.weba11y.server.domain.enums.InspectionItems;
import com.weba11y.server.api.dto.accessibilityViolation.AccessibilityViolationDto;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class PointerInputCancellationCheck extends AbstractAccessibilityChecker implements DynamicChecker {

    @Override
    protected InspectionItems getItem() {
        return InspectionItems.POINTER_INPUT_CANCELLATION;
    }

    @Override
    public List<AccessibilityViolationDto> checkDynamic(Page page, Long summaryId) {
        List<AccessibilityViolationDto> violations = new ArrayList<>();

        List<ElementHandle> elements = page.querySelectorAll("button, a, [data-pointer-action]");
        for (ElementHandle el : elements) {
            boolean supportsCancel = Boolean.TRUE.equals(page.evaluate("el => el.hasAttribute('data-cancelable')", el));
            if (!supportsCancel) {
                violations.add(AccessibilityViolationDto.createViolationDto(el, getItem(), summaryId));
                log.debug("[PointerInputCancellationCheck] 포인터 입력 취소 불가: element={}", el);
            }
        }
        return violations;
    }

    @Override
    protected boolean isViolation(Element element) {
        return false;
    }
}
