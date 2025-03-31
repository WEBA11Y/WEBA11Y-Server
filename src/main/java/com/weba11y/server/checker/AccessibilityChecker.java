package com.weba11y.server.checker;

import com.weba11y.server.dto.InspectionResults.InspectionResultDto;
import org.jsoup.nodes.Document;
import reactor.core.publisher.Flux;

public interface AccessibilityChecker {
    Flux<InspectionResultDto> check(Document doc);
}
