package com.weba11y.server.checker.implement;


import com.weba11y.server.checker.AccessibilityChecker;
import com.weba11y.server.domain.enums.InspectionItems;
import com.weba11y.server.dto.InspectionResults.InspectionResultDto;
import org.jsoup.nodes.Document;
import reactor.core.publisher.Flux;

public class LinearStructureCheck implements AccessibilityChecker {
    @Override
    public Flux<InspectionResultDto> check(Document doc) {
        return Flux.create(sink -> {
            // 콘텐츠의 선형 구조를 확인하는 로직 (예: <h1>, <h2>의 순서 검사)
            // 간단한 예시로, h1이 없을 경우를 확인
            if (doc.select("h1").isEmpty()) {
                InspectionResultDto result = InspectionResultDto.builder()
                        .inspectionItems(InspectionItems.LINEAR_STRUCTURE) // 적절한 Enum 값으로 설정
                        .summary("문서에 h1 태그가 없습니다.")
                        .codeLine(doc.outerHtml())
                        .build();
                sink.next(result);
            }
            sink.complete();
        });

    }
}

