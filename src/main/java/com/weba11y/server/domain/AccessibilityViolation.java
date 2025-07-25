package com.weba11y.server.domain;

import com.weba11y.server.domain.common.BaseEntity;
import com.weba11y.server.domain.enums.AccessibilityViolationStatus;
import com.weba11y.server.domain.enums.InspectionItems;
import com.weba11y.server.dto.accessibilityViolation.AccessibilityViolationDto;
import jakarta.persistence.*;
import lombok.*;

import static com.weba11y.server.domain.enums.AccessibilityViolationStatus.PENDING;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class AccessibilityViolation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InspectionItems inspectionItem;

    private int importance;

    private int assessmentLevel;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String codeLine;

    @Enumerated(EnumType.STRING)
    private AccessibilityViolationStatus status = PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inspection_summary_id")
    private InspectionSummary inspectionSummary;

    @Builder
    public AccessibilityViolation(
            InspectionItems inspectionItem,
            String description,
            String codeLine,
            InspectionSummary inspectionSummary
    ) {
        this.inspectionItem = inspectionItem;
        this.importance = inspectionItem.getImportance().getOrder();
        this.assessmentLevel = inspectionItem.getAssessmentLevel().getOrder();
        this.description = description;
        this.codeLine = codeLine;
        this.status = PENDING;
        this.inspectionSummary = inspectionSummary;
    }

    public void setInspectionSummary(InspectionSummary inspectionSummary) {
        this.inspectionSummary = inspectionSummary;
    }

    public void changeStatus(AccessibilityViolationStatus status) {
        this.status = status;
    }

    public AccessibilityViolationDto toDto() {
        return AccessibilityViolationDto.builder()
                .id(this.id)
                .inspectionItem(this.inspectionItem)
                .assessmentLevel(this.inspectionItem.getAssessmentLevel())
                .importance(this.inspectionItem.getImportance())
                .description(this.description)
                .codeLine(this.codeLine)
                .status(this.status)
                .createDate(this.getCreateDate())
                .updateDate(this.getUpdateDate())
                .build();
    }
}