package com.weba11y.server.application.service;

import com.weba11y.server.domain.violation.AccessibilityViolation;
import com.weba11y.server.domain.inspection.summary.InspectionSummary;
import com.weba11y.server.domain.inspection.url.InspectionUrl;
import com.weba11y.server.domain.enums.InspectionStatus;
import com.weba11y.server.api.dto.accessibilityViolation.AccessibilityViolationDto;
import com.weba11y.server.infrastructure.persistence.AccessibilityViolationRepository;
import com.weba11y.server.infrastructure.persistence.InspectionSummaryRepository;
import com.weba11y.server.infrastructure.persistence.InspectionUrlRepository;
import com.weba11y.server.application.service.InspectionPersistenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InspectionPersistenceServiceImpl implements InspectionPersistenceService {

    private final InspectionUrlRepository urlRepository;
    private final InspectionSummaryRepository summaryRepository;
    private final AccessibilityViolationRepository accessibilityViolationRepository;

    @Override
    @Transactional
    public InspectionSummary createAndPrepareInspectionSummary(Long inspectionUrlId) {
        log.info("[InspectionPersistenceService] Creating Inspection Summary for URL id: {}", inspectionUrlId);
        InspectionUrl inspectionUrl = urlRepository.findById(inspectionUrlId).orElseThrow(
                () -> new NoSuchElementException("InspectionUrl Not Found: " + inspectionUrlId)
        );
        InspectionSummary summary = InspectionSummary.builder()
                .inspectionUrl(inspectionUrl)
                .status(InspectionStatus.IN_PROGRESS)
                .build();
        return summaryRepository.save(summary);
    }

    @Override
    @Transactional
    public void updateInspectionStatus(Long summaryId, InspectionStatus status) {
        try {
            summaryRepository.findById(summaryId).ifPresent(summary -> {
                summary.updateStatus(status);
                summaryRepository.save(summary);
                log.info("[InspectionPersistenceService] InspectionSummary {} status updated to {}", summaryId, status);
            });
        } catch (Exception e) {
            log.error("[InspectionPersistenceService] Failed to update InspectionSummary status for id: {}", summaryId, e);
        }
    }

    @Override
    @Transactional
    public void updateInspectionSummary(InspectionSummary inspectionSummary, List<AccessibilityViolationDto> totalViolations) {
        List<AccessibilityViolation> violations = totalViolations.stream()
                .map(dto -> dto.toEntity(inspectionSummary))
                .collect(Collectors.toList());
        accessibilityViolationRepository.saveAll(violations);

        inspectionSummary.recalculateViolations();
        summaryRepository.save(inspectionSummary);
    }
}
