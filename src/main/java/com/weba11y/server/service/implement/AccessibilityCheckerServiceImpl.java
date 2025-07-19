package com.weba11y.server.service.implement;

import com.microsoft.playwright.Page;
import com.weba11y.server.checker.AccessibilityChecker;
import com.weba11y.server.checker.StaticContentAccessibilityChecker;
import com.weba11y.server.domain.InspectionSummary;
import com.weba11y.server.domain.InspectionUrl;
import com.weba11y.server.dto.accessibilityViolation.AccessibilityViolationDto;
import com.weba11y.server.dto.inspectionUrl.InspectionUrlDto;
import com.weba11y.server.repository.InspectionSummaryRepository;
import com.weba11y.server.repository.InspectionUrlRepository;
import com.weba11y.server.service.AccessibilityCheckerService;
import com.weba11y.server.service.PageLoaderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AccessibilityCheckerServiceImpl implements AccessibilityCheckerService {
    private final PageLoaderService pageLoaderService;
    private final StaticContentAccessibilityChecker staticContentAccessibilityChecker;
    private final InspectionUrlRepository urlRepository;
    private final InspectionSummaryRepository summaryRepository;

    @Override
    public List<AccessibilityViolationDto> runChecks(InspectionUrlDto inspectionUrl, SseEmitter sseEmitter) {
        Page page = pageLoaderService.getLoadedPage(inspectionUrl.getUrl());
        InspectionSummary inspectionSummary = createInspectionSummary(findInspectionUrl(inspectionUrl.getId()));
        Document document = Jsoup.parse(page.content());
        staticContentAccessibilityChecker.performCheck(document, sseEmitter, inspectionSummary);
        return null;
    }

    private InspectionUrl findInspectionUrl(Long inspectionUrlId) {
        return urlRepository.findById(inspectionUrlId).orElseThrow(
                () -> new NoSuchElementException("InspectionUrl Not Found")
        );
    }

    @Transactional
    public InspectionSummary createInspectionSummary(InspectionUrl inspectionUrl) {
        log.info("[StaticCheckerImpl] Creating Inspection Summary...");
        try {
            return summaryRepository.save(InspectionSummary.builder()
                    .inspectionUrl(inspectionUrl)
                    .build());
        } catch (Exception e) {
            log.error("Failed to create Inspection Summary: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create Inspection Summary", e);
        }
    }

}
