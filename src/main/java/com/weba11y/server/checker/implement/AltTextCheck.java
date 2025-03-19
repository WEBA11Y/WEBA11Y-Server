package com.weba11y.server.checker.implement;

import com.weba11y.server.checker.AccessibilityChecker;
import com.weba11y.server.dto.InspectionResults.InspectionResultDto;
import org.jsoup.nodes.Document;
import reactor.core.publisher.Mono;

public class AltTextCheck implements AccessibilityChecker {
    @Override
    public Mono<InspectionResultDto> check(Document doc) {

        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }
}
