package com.weba11y.server.checker.rule.perceivable;

import com.weba11y.server.checker.engine.AbstractAccessibilityChecker;
import com.weba11y.server.domain.enums.InspectionItems;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

@Component
public class TableStructureCheck extends AbstractAccessibilityChecker {
    @Override
    protected InspectionItems getItem() {
        return InspectionItems.TABLE_STRUCTURE;
    }

    @Override
    protected boolean isViolation(Element element) {
        return element.tagName().equals("table") &&
                element.select("th").isEmpty();  // 제목 셀 없는 경우
    }
}
