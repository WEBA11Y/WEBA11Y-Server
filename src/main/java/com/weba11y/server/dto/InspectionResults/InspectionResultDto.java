package com.weba11y.server.dto.InspectionResults;

import com.weba11y.server.domain.enums.AssessmentLevel;
import com.weba11y.server.domain.enums.Importance;
import com.weba11y.server.domain.enums.InspectionItems;
import lombok.*;


@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class InspectionResultDto {
    // 검사 결과 Index
    private Long id;

    // 검사 항목 번호
    private int number;

    private String title;

    private Importance importance;

    private AssessmentLevel assessmentLevel;

    private String summary;

    @Builder
    public InspectionResultDto(Long id, InspectionItems inspectionItems, String summary) {
        this.id = id;
        this.number = inspectionItems.getNumber();
        this.title = inspectionItems.getName();
        this.importance = inspectionItems.getImportance();
        this.assessmentLevel = inspectionItems.getAssessmentLevel();
        this.summary = summary;
    }
}
