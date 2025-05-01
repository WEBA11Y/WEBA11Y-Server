package com.weba11y.server.checker.implement;

import com.weba11y.server.checker.AccessibilityChecker;
import com.weba11y.server.dto.InspectionResults.InspectionResultDto;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import static com.weba11y.server.domain.enums.InspectionItems.*;

@Slf4j
@Component
public class AltTextCheck implements AccessibilityChecker {

    @Override
    public Flux<InspectionResultDto> check(Document doc) {
        return Flux.fromIterable(ALT_TEXT.getTargetTags())
                .flatMap(tag -> Flux.fromIterable(doc.select(tag)))
                .filter(element -> !element.hasAttr("alt") || element.attr("alt").isEmpty())
                .map(element -> {
                    log.debug("[AltTextCheck] ALT 누락 요소 발견: <{}>", element.tagName());
                    return InspectionResultDto.createInspectionResultDto(element, ALT_TEXT);
                });
    }
}




