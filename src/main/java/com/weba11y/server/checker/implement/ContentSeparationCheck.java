package com.weba11y.server.checker.implement;

import com.weba11y.server.checker.AccessibilityChecker;
import com.weba11y.server.dto.InspectionResults.InspectionResultDto;
import org.jsoup.nodes.Document;
import reactor.core.publisher.Flux;

import static com.weba11y.server.domain.enums.InspectionItems.*;

public class ContentSeparationCheck implements AccessibilityChecker {
    @Override
    public Flux<InspectionResultDto> check(Document doc) {
        return Flux.create(sink -> {
            // 콘텐츠 간의 구분을 검사하는 로직
            // 예시: 적절한 구분이 없는 경우
            if (doc.select("hr").isEmpty() && doc.select("div").size() > 5) {
                InspectionResultDto result = InspectionResultDto.builder()
                        .inspectionItems(CONTENT_SEPARATION) // 적절한 Enum 값으로 설정
                        .summary("콘텐츠 간의 구분이 명확하지 않습니다.")
                        .codeLine(doc.outerHtml())
                        .build();
                sink.next(result);
            }
            sink.complete();
        });
    }
}