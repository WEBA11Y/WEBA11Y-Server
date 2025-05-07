package com.weba11y.server.checker.implement;

import com.weba11y.server.checker.AccessibilityChecker;
import com.weba11y.server.dto.InspectionResults.InspectionResultDto;
import org.jsoup.nodes.Document;
import reactor.core.publisher.Flux;

import static com.weba11y.server.domain.enums.InspectionItems.*;

public class TextContrastCheck implements AccessibilityChecker {
    @Override
    public Flux<InspectionResultDto> check(Document doc) {
        return Flux.create(sink -> {
            // 텍스트 대비 검사 로직
            // 예시: 특정 색상 조합을 검사 (여기서는 간단한 예시)
            if (doc.select("[style*='color']").size() > 0) {
                InspectionResultDto result = InspectionResultDto.builder()
                        .inspectionItems(TEXT_CONTRAST) // 적절한 Enum 값으로 설정
                        .summary("텍스트 대비가 불충분할 수 있습니다.")
                        .codeLine(doc.outerHtml())
                        .build();
                sink.next(result);
            }
            sink.complete();
        });
    }

}