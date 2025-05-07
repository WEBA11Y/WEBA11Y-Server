package com.weba11y.server.checker.implement;

import com.weba11y.server.checker.AccessibilityChecker;
import com.weba11y.server.domain.enums.InspectionItems;
import com.weba11y.server.dto.InspectionResults.InspectionResultDto;
import org.jsoup.nodes.Document;
import reactor.core.publisher.Flux;

public class ColorIndependenceCheck implements AccessibilityChecker {
    @Override
    public Flux<InspectionResultDto> check(Document doc) {
        return Flux.create(sink -> {
            // 색에 의존하는 콘텐츠 체크 로직
            // 예시: 색상만으로 정보를 제공하는 경우
            if (doc.select("[style*='color']").size() > 0) {
                InspectionResultDto result = InspectionResultDto.builder()
                        .inspectionItems(InspectionItems.COLOR_INDEPENDENCE) // 적절한 Enum 값으로 설정
                        .summary("색상에 의존하는 콘텐츠가 있습니다.")
                        .codeLine(doc.outerHtml())
                        .build();
                sink.next(result);
            }
            sink.complete();
        });
    }


}