package com.weba11y.server.service.implement;

import com.weba11y.server.domain.AccessibilityViolation;
import com.weba11y.server.domain.enums.AssessmentLevel;
import com.weba11y.server.domain.enums.Importance;
import com.weba11y.server.domain.enums.InspectionItems;
import com.weba11y.server.dto.accessibilityViolation.AccessibilityViolationDto;
import com.weba11y.server.repository.AccessibilityViolationRepository;
import com.weba11y.server.service.AccessibilityViolationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
@Transactional(value = "transactionManager", readOnly = true)
@RequiredArgsConstructor
public class AccessibilityViolationServiceImpl implements AccessibilityViolationService {

    private final AccessibilityViolationRepository accessibilityViolationRepository;


    @Value("${page.result.size}")
    private int size;

    @Override
    public List<AccessibilityViolationDto> getViolationsByInspectionSummaryId(Long inspectionSummaryId) {
        return accessibilityViolationRepository.findByInspectionSummaryId(inspectionSummaryId)
                .stream()
                .map(AccessibilityViolation::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public AccessibilityViolationDto.AccessibilityViolationsResponse getViolationsByImportance(
            int page,
            Long inspectionSummaryId,
            Importance importance) {
        return getViolations(page, inspectionSummaryId, InspectionItems.findItemsByImportance(importance));
    }

    @Override
    public AccessibilityViolationDto.AccessibilityViolationsResponse getViolationsByAssessmentLevel(
            int page,
            Long inspectionSummaryId,
            AssessmentLevel assessmentLevel) {
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
}
