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
public class FlashPreventionCheck extends AbstractAccessibilityChecker implements DynamicChecker {

    @Override
    protected InspectionItems getItem() {
        return InspectionItems.FLASH_PREVENTION;
    }

    @Override
    public List<AccessibilityViolationDto> checkDynamic(Page page, Long summaryId) {
        List<AccessibilityViolationDto> violations = new ArrayList<>();

        // CSS animation 또는 blink 효과 감지
        List<ElementHandle> elements = page.querySelectorAll("*");
        for (ElementHandle el : elements) {
            String animation = page.evaluate("el => getComputedStyle(el).animationName", el).toString();
            String freq = page.evaluate("el => getComputedStyle(el).animationDuration", el).toString();

            if (!"none".equals(animation) && isFlashing(freq)) {
                violations.add(AccessibilityViolationDto.createViolationDto(el, getItem(), summaryId));
                log.debug("[FlashPreventionCheck] 깜빡임 감지: element={}", el);
            }
        }
        return violations;
    }

    private boolean isFlashing(String durationCss) {
        try {
            double seconds = Double.parseDouble(durationCss.replace("s", ""));
            double hz = 1 / seconds;
            return hz >= 3 && hz <= 50;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    protected boolean isViolation(Element element) {
        return false;
    }
}

