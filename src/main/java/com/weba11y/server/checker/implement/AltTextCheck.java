package com.weba11y.server.checker.implement;

import com.weba11y.server.checker.AccessibilityChecker;
import com.weba11y.server.dto.InspectionResults.InspectionResultDto;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import reactor.core.publisher.Flux;

import static com.weba11y.server.domain.enums.InspectionItems.*;

@Slf4j
public class AltTextCheck implements AccessibilityChecker {
    @Override
    public Flux<InspectionResultDto> check(Document doc) {
        return Flux.create(sink -> {
            ALT_TEXT.getTargetTags().forEach(tag -> {
                doc.select(tag).stream()
                        .filter(element -> !element.hasAttr("alt") || element.attr("alt").isEmpty())
                        .map(element -> InspectionResultDto.createInspectionResultDto(element, ALT_TEXT))
                        .forEach(sink::next); // 결과를 스트림으로 전송
            });
            sink.complete(); // 모든 검사 완료
        });
    }
}



