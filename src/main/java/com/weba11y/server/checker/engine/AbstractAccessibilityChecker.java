package com.weba11y.server.checker.engine;

import com.weba11y.server.domain.enums.InspectionItems;
import com.weba11y.server.api.dto.accessibilityViolation.AccessibilityViolationDto;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public abstract class AbstractAccessibilityChecker implements AccessibilityChecker {

    protected abstract InspectionItems getItem();  // 해당 검사 항목 반환

    @Override
    public List<AccessibilityViolationDto> check(Document doc, Long inspectionSummaryId) {
        List<AccessibilityViolationDto> violations = new ArrayList<>();

        for (String target : getItem().getTargetTags()) {
            List<AccessibilityViolationDto> found = doc.select(target).stream()
                    .filter(this::isViolation) // 항목별 위반 조건
                    .map(element -> {
                        log.debug("[{}] 위반 요소 발견: <{}>", getItem().getName(), element.tagName());
                        return AccessibilityViolationDto.createViolationDto(element, getItem(), inspectionSummaryId);
                    })
                    .toList();
            violations.addAll(found);
        }

        return violations;
    }

    /**
     * 항목별 위반 조건 구현 (각 하위 클래스에서 정의)
     */
    protected abstract boolean isViolation(Element element);
}
