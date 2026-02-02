package com.weba11y.server.checker.rule.perceivable;

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
public class TextContrastCheck extends AbstractAccessibilityChecker implements DynamicChecker {

    @Override
    protected InspectionItems getItem() {
        return InspectionItems.TEXT_CONTRAST;
    }

    @Override
    protected boolean isViolation(Element element) {
        return false;
    }

    @Override
    public List<AccessibilityViolationDto> checkDynamic(Page page, Long summaryId) {
        List<AccessibilityViolationDto> violations = new ArrayList<>();

        List<ElementHandle> elements = page.querySelectorAll("p, span, label");
        for (ElementHandle el : elements) {
            try {
                String fg = page.evaluate("el => getComputedStyle(el).color", el).toString();
                String bg = page.evaluate("el => getComputedStyle(el).backgroundColor", el).toString();

                double ratio = ContrastCalculator.calculate(fg, bg);
                if (!ContrastCalculator.isContrastSufficient(ratio, false)) {
                    violations.add(AccessibilityViolationDto.createViolationDto(el, getItem(), summaryId));
                    log.debug("[TextContrastCheck] 명도 대비 위반: ratio={}, element={}", ratio, el);
                } else {
                    el.dispose();
                }
            } catch (Exception e) {
                try { el.dispose(); } catch (Exception ignore) {}
            }
        }
        return violations;
    }

}
