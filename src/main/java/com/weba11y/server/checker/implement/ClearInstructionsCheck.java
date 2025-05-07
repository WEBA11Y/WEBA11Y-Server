/*
package com.weba11y.server.checker.implement;

import com.weba11y.server.checker.AccessibilityChecker;
import com.weba11y.server.dto.InspectionResults.InspectionResultDto;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import reactor.core.publisher.Flux;


public class ClearInstructionsCheck implements AccessibilityChecker {
    @Override
    public Flux<InspectionResultDto> check(Document doc) {
        return Flux.create(sink -> {
            doc.select("button, a, label, input").stream()
                    .filter(element -> !hasValidAttributes(element))
                    .map(element -> InspectionResultDto.createInspectionResultDto(element, ))
                    .forEach(sink::next);

            sink.complete();
        });
    }

    private boolean hasValidAttributes(Element element) {
        return element.hasText() || element.hasAttr("aria-label") ||
                element.hasAttr("aria-describedby") || element.hasAttr("title");
    }

}
*/
