package com.weba11y.server.application.service;

import com.weba11y.server.domain.inspection.summary.InspectionSummary;
import com.weba11y.server.domain.enums.InspectionStatus;
import com.weba11y.server.api.dto.accessibilityViolation.AccessibilityViolationDto;

import java.util.List;

public interface InspectionPersistenceService {

    InspectionSummary createAndPrepareInspectionSummary(Long inspectionUrlId);

    void updateInspectionStatus(Long summaryId, InspectionStatus status);

    void updateInspectionSummary(InspectionSummary inspectionSummary, List<AccessibilityViolationDto> totalViolations);
}
