package com.weba11y.server.domain;


import com.weba11y.server.domain.common.BaseEntity;
import com.weba11y.server.dto.inspectionSummary.InspectionSummaryDto;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.weba11y.server.domain.enums.AccessibilityViolationStatus.*;


@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class InspectionSummary extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 전체 위반 개수
    @Builder.Default
    private Long totalViolations = 0L;

    // 해결 된 위반 개수
    @Builder.Default
    private Long completedViolations = 0L;

    // 미완료된 위반 개수
    @Builder.Default
    private Long pendingViolations = 0L;

    // 오류로 분류된 위반 개수
    @Builder.Default
    private Long errorViolations = 0L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inspection_url_id")
    private InspectionUrl inspectionUrl;

    @OneToMany(mappedBy = "inspectionSummary", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @BatchSize(size = 100)
    @Builder.Default
    private List<AccessibilityViolation> accessibilityViolations = new ArrayList<>();

    public void setTotalViolations(Long count) {
        this.totalViolations = count;
    }

    public void setCompletedViolations(Long count) {
        this.completedViolations = count;
    }

    public void setPendingViolations(Long count) {
        this.pendingViolations = count;
    }

    public void setErrorViolations(Long count) {
        this.errorViolations = count;
    }

    public void addAccessibilityViolation(AccessibilityViolation accessibilityViolation) {
        this.accessibilityViolations.add(accessibilityViolation);
    }

    public InspectionSummaryDto toDto() {

        return InspectionSummaryDto.builder()
                .id(this.id)
                .inspectionUrlId(this.inspectionUrl.getId())
                .violations(toViolationStatisticsDto(this.accessibilityViolations))
                .createDate(this.getCreateDate())
                .updateDate(this.getUpdateDate())
                .build();
    }

    public InspectionSummaryDto.InspectionSummaryMetadataDto toMetadataDto() {
        return InspectionSummaryDto.InspectionSummaryMetadataDto.builder()
                .id(this.id)
                .createDate(this.getCreateDate())
                .updateDate(this.getUpdateDate())
                .build();
    }


    public static InspectionSummaryDto.ViolationStatisticsDto toViolationStatisticsDto(List<AccessibilityViolation> violations) {
        // 상태별 전체 집계
        long total = violations.size();
        long completed = violations.stream().filter(v -> v.getStatus() == COMPLETED).count();
        long pending = violations.stream().filter(v -> v.getStatus() == PENDING).count();
        long error = violations.stream().filter(v -> v.getStatus() == ERROR).count();

        // 중요도별 집계
        Map<String, List<AccessibilityViolation>> byImportance = violations.stream()
                .collect(Collectors.groupingBy(v -> v.getInspectionItem().getImportance().name()));

        InspectionSummaryDto.ImportanceBreakdownDto importance = InspectionSummaryDto.ImportanceBreakdownDto.builder()
                .critical(toViolationCountDetail(byImportance.get("CRITICAL")))
                .serious(toViolationCountDetail(byImportance.get("SERIOUS")))
                .moderate(toViolationCountDetail(byImportance.get("MODERATE")))
                .minor(toViolationCountDetail(byImportance.get("MINOR")))
                .build();

        // 평가 레벨별 집계
        Map<String, List<AccessibilityViolation>> byAssessment = violations.stream()
                .collect(Collectors.groupingBy(v -> v.getInspectionItem().getAssessmentLevel().name()));

        InspectionSummaryDto.AssessmentLevelBreakdownDto assessment = InspectionSummaryDto.AssessmentLevelBreakdownDto.builder()
                .A(toViolationCountDetail(byAssessment.get("A")))
                .AA(toViolationCountDetail(byAssessment.get("AA")))
                .AAA(toViolationCountDetail(byAssessment.get("AAA")))
                .BEST(toViolationCountDetail(byAssessment.get("BEST")))
                .build();

        return InspectionSummaryDto.ViolationStatisticsDto.builder()
                .totalViolations(total)
                .completedViolations(completed)
                .pendingViolations(pending)
                .errorViolations(error)
                .importance(importance)
                .assessmentLevel(assessment)
                .build();
    }

    private static InspectionSummaryDto.ViolationCountDetail toViolationCountDetail(List<AccessibilityViolation> violations) {
        if (violations == null) return emptyViolationCountDetail();
        long total = violations.size();
        long completed = violations.stream().filter(v -> v.getStatus() == COMPLETED).count();
        long pending = violations.stream().filter(v -> v.getStatus() == PENDING).count();
        long error = violations.stream().filter(v -> v.getStatus() == ERROR).count();

        return InspectionSummaryDto.ViolationCountDetail.builder()
                .total(total)
                .completed(completed)
                .pending(pending)
                .error(error)
                .build();
    }

    private static InspectionSummaryDto.ViolationCountDetail emptyViolationCountDetail() {
        return InspectionSummaryDto.ViolationCountDetail.builder()
                .total(0).completed(0).pending(0).error(0)
                .build();
    }

}

