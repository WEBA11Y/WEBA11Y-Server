package com.weba11y.server.checker.implement;

import com.weba11y.server.checker.AbstractAccessibilityChecker;
import com.weba11y.server.domain.enums.InspectionItems;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

@Component
public class AutoPlayCheck extends AbstractAccessibilityChecker {

    @Override
    protected InspectionItems getItem() {
        return InspectionItems.AUTO_PLAY;
    }

    @Override
    protected boolean isViolation(Element element) {
        return element.hasAttr("autoplay");
    }
}

