package com.weba11y.server.checker.implement;

import com.weba11y.server.checker.AccessibilityChecker;
import com.weba11y.server.domain.enums.InspectionItems;
import com.weba11y.server.dto.InspectionResults.InspectionResultDto;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import reactor.core.publisher.Flux;

import java.util.List;

import static com.weba11y.server.domain.enums.InspectionItems.TABLE_STRUCTURE;

public class TableStructureCheck implements AccessibilityChecker {
    @Override
    public Flux<InspectionResultDto> check(Document doc) {
        return Flux.create(sink -> {
            TABLE_STRUCTURE.getTargetTags().forEach(tag -> {
                doc.select(tag).stream()
                        .filter(element -> element.select("th").isEmpty())
                        .map(element -> createInspectionResult(element))
                        .forEach(sink::next);
            });
            sink.complete();
        });
    }

    private static InspectionResultDto createInspectionResult(Element element) {
        return InspectionResultDto.builder()
                .inspectionItems(TABLE_STRUCTURE) // 적절한 Enum 값으로 설정
                .summary("표에 헤더(th) 요소가 없습니다.")
                .codeLine(element.outerHtml())
                .build();
    }
}
