package com.weba11y.server.application.service;

import com.weba11y.server.domain.violation.AccessibilityViolation;
import com.weba11y.server.domain.inspection.summary.InspectionSummary;
import com.weba11y.server.domain.enums.AccessibilityViolationStatus;
import com.weba11y.server.domain.enums.InspectionItems;
import com.weba11y.server.api.dto.accessibilityViolation.AccessibilityViolationDto;
import com.weba11y.server.infrastructure.persistence.AccessibilityViolationRepository;
import com.weba11y.server.infrastructure.persistence.InspectionSummaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AccessibilityViolationServiceImpl implements AccessibilityViolationService {

    private final AccessibilityViolationRepository accessibilityViolationRepository;
    private final InspectionSummaryRepository inspectionSummaryRepository;

    @Value("${page.result.size}")
    private int size;

    @Override
    public AccessibilityViolationDto.AccessibilityViolationsResponse getViolationsByImportance(
            int page,
            Long inspectionSummaryId,
            String importance) {
        return getViolations(page, inspectionSummaryId, InspectionItems.findItemsByImportance(importance));
    }

    @Override
    public AccessibilityViolationDto.AccessibilityViolationsResponse getViolationsByAssessmentLevel(
            int page,
            Long inspectionSummaryId,
            String assessmentLevel) {
        return getViolations(page, inspectionSummaryId, InspectionItems.findItemsByAssessmentLevel(assessmentLevel));
    }

    @Override
    public List<AccessibilityViolationDto> getTop5ViolationsBySummaryId(Long inspectionSummaryId) {
        return accessibilityViolationRepository.findTop5BySummaryIdOrderByPriority(inspectionSummaryId)
                .stream()
                .map(av -> av.toDto())
                .toList();
    }

    private AccessibilityViolationDto.AccessibilityViolationsResponse getViolations(int page, Long inspectionSummaryId, List<InspectionItems> items) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<AccessibilityViolation> list = accessibilityViolationRepository.findByInspectionSummaryIdAndItems(pageable, inspectionSummaryId, items);
        return AccessibilityViolationDto.AccessibilityViolationsResponse.builder()
                .content(list.stream().map(AccessibilityViolation::toDto).collect(Collectors.toList()))
                .totalPage(list.getTotalPages())
                .currentPage(page)
                .build();
    }

    @Override
    @Transactional
    public AccessibilityViolation save(AccessibilityViolationDto accessibilityViolationDto) {
        InspectionSummary inspectionSummary = inspectionSummaryRepository.findById(accessibilityViolationDto.getInspectionSummaryId())
                .orElseThrow(() -> new NoSuchElementException("InspectionSummary not found"));
        AccessibilityViolation accessibilityViolation = accessibilityViolationDto.toEntity(inspectionSummary);
        inspectionSummary.addViolation(accessibilityViolation);
        inspectionSummary.recalculateViolations();
        return accessibilityViolationRepository.save(accessibilityViolation);
    }

    @Override
    @Transactional
    public AccessibilityViolation updateViolationStatus(Long violationId, AccessibilityViolationStatus status) {
        AccessibilityViolation violation = accessibilityViolationRepository.findById(violationId)
                .orElseThrow(() -> new NoSuchElementException("AccessibilityViolation not found with id: " + violationId));
        violation.changeStatus(status);

        InspectionSummary summary = violation.getInspectionSummary();
        summary.recalculateViolations();

        return violation;
    }
}
