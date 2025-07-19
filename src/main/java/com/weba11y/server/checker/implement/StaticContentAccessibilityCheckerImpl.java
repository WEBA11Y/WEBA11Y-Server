package com.weba11y.server.checker.implement;

import com.weba11y.server.checker.SseEventSender;
import com.weba11y.server.checker.StaticContentAccessibilityChecker;
import com.weba11y.server.domain.InspectionSummary;
import com.weba11y.server.dto.accessibilityViolation.AccessibilityViolationDto;
import com.weba11y.server.repository.AccessibilityViolationRepository;
import com.weba11y.server.repository.InspectionSummaryRepository;
import com.weba11y.server.repository.InspectionUrlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.weba11y.server.domain.enums.InspectionItems.ALT_TEXT;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StaticContentAccessibilityCheckerImpl implements StaticContentAccessibilityChecker {

    private final AccessibilityViolationRepository accessibilityViolationRepository;
    private final SseEventSender sseEventSender;

    @Override
    @Async("taskExecutor")
    public CompletableFuture<Void> performCheck(Document document, SseEmitter emitter, InspectionSummary inspectionSummary) {
        log.info("[StaticCheckerImpl] Starting static content accessibility check.");
        altTextCheck(document, emitter, inspectionSummary);
        return CompletableFuture.completedFuture(null);
    }

    private void altTextCheck(Document document, SseEmitter emitter, InspectionSummary inspectionSummary) {
        log.info("[StaticCheckerImpl] Start alternative text accessibility check...");
        List<AccessibilityViolationDto> violations = new ArrayList<>();
        try {
            for (String target : ALT_TEXT.getTargetTags()) {
                for (Element element : document.select(target)) {
                    if (!element.hasAttr("alt") || element.attr("alt").isEmpty()) {
                        AccessibilityViolationDto violation = AccessibilityViolationDto.createInspectionResultDto(element, ALT_TEXT);
                        violations.add(violation);
                        sseEventSender.sendViolationEvent(emitter, violation);
                    }
                }
            }
            saveViolations(violations, inspectionSummary);
        } catch (Exception e) {
            sseEventSender.sendErrorEvent(emitter, "Static content check failed: " + e.getMessage());
        }
    }

    @Async
    @Transactional
    protected void saveViolations(List<AccessibilityViolationDto> violations, InspectionSummary inspectionSummary) {
        accessibilityViolationRepository.saveAll(violations.stream()
                .map(violation -> violation.toEntity(inspectionSummary)).collect(Collectors.toList()));
    }


}
