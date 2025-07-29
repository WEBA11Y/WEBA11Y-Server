package com.weba11y.server.checker.implement;

import com.weba11y.server.checker.AbstractAccessibilityChecker;
import com.weba11y.server.domain.enums.InspectionItems;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

@Component
public class MultimediaAlternativeCheck extends AbstractAccessibilityChecker {

    @Override
    protected InspectionItems getItem() {
        return InspectionItems.MULTIMEDIA_ALTERNATIVE;
    }

    @Override
    protected boolean isViolation(Element element) {
        boolean hasCaptions = !element.select("track[kind=captions], track[kind=subtitles]").isEmpty();
        boolean hasTranscript = element.hasAttr("data-transcript") ||
                (element.nextElementSibling() != null &&
                        element.nextElementSibling().tagName().equalsIgnoreCase("div") &&
                        element.nextElementSibling().hasClass("transcript"));
        return !hasCaptions && !hasTranscript;
    }
}

