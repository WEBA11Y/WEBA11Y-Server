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
public class FocusIndicationCheck extends AbstractAccessibilityChecker implements DynamicChecker {

    @Override
    protected InspectionItems getItem() {
        return InspectionItems.FOCUS_INDICATION;
    }

    @Override
    public List<AccessibilityViolationDto> checkDynamic(Page page, Long summaryId) {
        List<AccessibilityViolationDto> violations = new ArrayList<>();

        List<ElementHandle> focusable = page.querySelectorAll("a, button, input, [tabindex]");
        for (ElementHandle el : focusable) {
            try {
                page.evaluate("el => el.focus()", el);
                String outline = page.evaluate("el => getComputedStyle(el).outlineWidth", el).toString();
                String boxShadow = page.evaluate("el => getComputedStyle(el).boxShadow", el).toString();

                boolean visible = !"0px".equals(outline) || (boxShadow != null && !boxShadow.isBlank());
                if (!visible) {
                    violations.add(AccessibilityViolationDto.createViolationDto(el, getItem(), summaryId));
                    log.debug("[FocusIndicationCheck] 초점 표시 없음: element={}", el);
                } else {
                    el.dispose();
                }
            } catch (Exception e) {
                try { el.dispose(); } catch (Exception ignore) {}
            }
        }
        return violations;
    }

    @Override
    protected boolean isViolation(Element element) { return false; }
}
