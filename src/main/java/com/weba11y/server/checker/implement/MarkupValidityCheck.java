package com.weba11y.server.checker.implement;

import com.weba11y.server.checker.AbstractAccessibilityChecker;
import com.weba11y.server.domain.enums.InspectionItems;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

@Component
public class MarkupValidityCheck extends AbstractAccessibilityChecker {
    @Override
    protected InspectionItems getItem() {
        return InspectionItems.MARKUP_VALIDITY;
    }

    @Override
    protected boolean isViolation(Element element) {
        // Jsoup Parser를 이용한 마크업 검증
        try {
            Jsoup.parse(element.outerHtml());
            return false;
        } catch (Exception e) {
            return true;
        }
    }
}
