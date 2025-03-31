package com.weba11y.server.checker.implement;

import com.weba11y.server.checker.AccessibilityChecker;
import com.weba11y.server.dto.InspectionResults.InspectionResultDto;
import org.jsoup.nodes.Document;
import reactor.core.publisher.Flux;

import static com.weba11y.server.domain.enums.InspectionItems.*;

public class AltMultimediaCheck implements AccessibilityChecker {
    @Override
    public Flux<InspectionResultDto> check(Document doc) {
        return Flux.create(sink -> {
            ALT_MULTIMEDIA.getTargetTags().forEach(tag -> {
                doc.select(tag).stream()
                        .filter(element -> !element.hasAttr("alt") || element.attr("alt").isEmpty())
                        .map(element -> InspectionResultDto.createInspectionResultDto(element, ALT_MULTIMEDIA))
                        .forEach(sink::next);
            });
            sink.complete();
        });
    }
}
