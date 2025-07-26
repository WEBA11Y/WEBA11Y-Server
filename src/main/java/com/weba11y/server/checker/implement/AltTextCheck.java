package com.weba11y.server.checker.implement;

import com.weba11y.server.checker.AccessibilityChecker;
import com.weba11y.server.dto.accessibilityViolation.AccessibilityViolationDto;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.weba11y.server.domain.enums.InspectionItems.*;

@Slf4j
@Component
public class AltTextCheck implements AccessibilityChecker {

    @Override
    public List<AccessibilityViolationDto> check(Document doc, Long inspectionSummaryId) {
        List<AccessibilityViolationDto> violations = new ArrayList<>();

        for (String target : ALT_TEXT.getTargetTags()) {
            List<AccessibilityViolationDto> tagViolations = doc.select(target)
                    .stream()
                    .filter(element -> !element.hasAttr("alt") || element.attr("alt").isEmpty())
                    .map(element -> {
                        log.debug("[AltTextCheck] ALT 누락 요소 발견: <{}>", element.tagName());
                        return AccessibilityViolationDto.createInspectionResultDto(element, ALT_TEXT, inspectionSummaryId);
                    }).collect(Collectors.toList());
            violations.addAll(tagViolations);
        }
        return violations;
    }
}




