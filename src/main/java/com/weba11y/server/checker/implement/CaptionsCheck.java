/*
package com.weba11y.server.checker.implement;

import com.weba11y.server.checker.AccessibilityChecker;
import com.weba11y.server.dto.InspectionResults.InspectionResultDto;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import reactor.core.publisher.Flux;


import static com.weba11y.server.domain.enums.InspectionItems.*;

public class CaptionsCheck implements AccessibilityChecker {
    @Override
    public Flux<InspectionResultDto> check(Document doc) {
        return Flux.create(sink -> {
            C.getTargetTags().forEach(tag -> {
                doc.select(tag).stream()
                        .filter(element -> element.select("track").isEmpty())
                        .map(element -> ))
                        .forEach(sink::next);
            });
            sink.complete();
        });
    }

    @Override
    public String getDescription() {
        return null;
    }
}

*/
