package com.weba11y.server.checker.rule.operable;

import com.weba11y.server.checker.engine.AbstractAccessibilityChecker;
import com.weba11y.server.domain.enums.InspectionItems;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

@Component
public class KeyboardAccessibilityCheck extends AbstractAccessibilityChecker {
    @Override
    protected InspectionItems getItem() {
        return InspectionItems.KEYBOARD_ACCESSIBILITY;
    }

    @Override
    protected boolean isViolation(Element element) {
        return element.hasAttr("onclick") &&
                !element.hasAttr("tabindex") &&
                !element.hasAttr("onkeypress") &&
                !element.tagName().equalsIgnoreCase("a");
    }
}

