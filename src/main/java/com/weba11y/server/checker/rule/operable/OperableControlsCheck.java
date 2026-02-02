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
public class OperableControlsCheck extends AbstractAccessibilityChecker implements DynamicChecker {

    @Override
    protected InspectionItems getItem() {
        return InspectionItems.OPERABLE_CONTROLS;
    }

    @Override
    public List<AccessibilityViolationDto> checkDynamic(Page page, Long summaryId) {
        List<AccessibilityViolationDto> violations = new ArrayList<>();

        List<ElementHandle> controls = page.querySelectorAll("a, button, input[type=button], input[type=submit]");
        for (ElementHandle el : controls) {
            try {
                int width = Integer.parseInt(page.evaluate("el => el.offsetWidth", el).toString());
                int height = Integer.parseInt(page.evaluate("el => el.offsetHeight", el).toString());

                if (width < 44 || height < 44) {
                    violations.add(AccessibilityViolationDto.createViolationDto(el, getItem(), summaryId));
                    log.debug("[OperableControlsCheck] 조작 영역 부족: {}x{}, element={}", width, height, el);
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
    protected boolean isViolation(Element element) {
        return false;
    }
}
