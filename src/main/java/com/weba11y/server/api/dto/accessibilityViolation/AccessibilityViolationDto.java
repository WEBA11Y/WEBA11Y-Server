package com.weba11y.server.api.dto.accessibilityViolation;

import com.microsoft.playwright.ElementHandle;
import com.weba11y.server.domain.violation.AccessibilityViolation;
import com.weba11y.server.domain.inspection.summary.InspectionSummary;
import com.weba11y.server.domain.enums.AssessmentLevel;
import com.weba11y.server.domain.enums.Importance;
import com.weba11y.server.domain.enums.InspectionItems;
import com.weba11y.server.domain.enums.AccessibilityViolationStatus;
import lombok.*;
import org.jsoup.nodes.Element;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.weba11y.server.domain.enums.AccessibilityViolationStatus.*;


@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class AccessibilityViolationDto {

    private Long id;

    private Long inspectionSummaryId;

    private InspectionItems inspectionItem;

    private AssessmentLevel assessmentLevel;

    private Importance importance;

    private String description;

    private String codeLine;

    private AccessibilityViolationStatus status;

    private LocalDateTime createDate;

    private LocalDateTime updateDate;


    public static AccessibilityViolationDto createViolationDto(Element element, InspectionItems inspectionItem, Long inspectionSummaryId) {
        String codeLine = element.outerHtml();
        if (codeLine.length() > 255) {
            codeLine = codeLine.substring(0, 255); // 255자까지 자르기
        }

        return getBuild(inspectionItem, inspectionSummaryId, codeLine);
    }

    public static AccessibilityViolationDto createViolationDto(ElementHandle element, InspectionItems inspectionItem, Long inspectionSummaryId) {
        String codeLine;

        try {
            // Playwright evaluate()로 해당 요소의 outerHTML 가져오기
            codeLine = element.evaluate("el => el.outerHTML").toString();
        } catch (Exception e) {
            codeLine = "<unavailable>"; // HTML 추출 실패 시 기본값
        }finally {
            element.dispose();
        }

        if (codeLine.length() > 255) {
            codeLine = codeLine.substring(0, 255);
        }

        return getBuild(inspectionItem, inspectionSummaryId, codeLine);
    }

    private static AccessibilityViolationDto getBuild(InspectionItems inspectionItem, Long inspectionSummaryId, String codeLine) {
        return AccessibilityViolationDto.builder()
                .inspectionSummaryId(inspectionSummaryId)
                .inspectionItem(inspectionItem)
                .assessmentLevel(inspectionItem.getAssessmentLevel())
                .importance(inspectionItem.getImportance())
                .description(inspectionItem.getDescription())
                .codeLine(codeLine)
                .status(PENDING)
                .createDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .build();
    }

    public AccessibilityViolation toEntity(InspectionSummary inspectionSummary) {
        return AccessibilityViolation.builder()
                .inspectionSummary(inspectionSummary)
                .inspectionItem(this.inspectionItem)
                .description(this.description)
                .codeLine(this.codeLine)
                .build();
    }

    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    public static class AccessibilityViolationsResponse {
        @Builder.Default
        private List<AccessibilityViolationDto> content = new ArrayList<>();
        private int totalPage;
        private int currentPage;
    }
}
