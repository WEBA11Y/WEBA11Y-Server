package com.weba11y.server.checker.rule.perceivable;

import com.weba11y.server.checker.engine.AbstractAccessibilityChecker;
import com.weba11y.server.domain.enums.InspectionItems;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

@Component
public class AltTextCheck extends AbstractAccessibilityChecker {

    @Override
    protected InspectionItems getItem() {
        return InspectionItems.ALT_TEXT;
    }

    @Override
    protected boolean isViolation(Element element) {
        return !element.hasAttr("alt") || element.attr("alt").isBlank();
    }
}




