package com.weba11y.server.dto.accessibilityViolation;

import com.weba11y.server.domain.AccessibilityViolation;
import com.weba11y.server.domain.InspectionSummary;
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


    public static AccessibilityViolationDto createInspectionResultDto(Element element, InspectionItems inspectionItem, Long inspectionSummaryId) {
        String codeLine = element.outerHtml();
        if (codeLine.length() > 255) {
            codeLine = codeLine.substring(0, 255); // 255자까지 자르기
        }

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
