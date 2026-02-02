package com.weba11y.server.domain.inspection.summary;

import com.weba11y.server.domain.common.BaseEntity;
import com.weba11y.server.domain.enums.AccessibilityViolationStatus;
import com.weba11y.server.domain.inspection.url.InspectionUrl;
import com.weba11y.server.domain.violation.AccessibilityViolation;
import com.weba11y.server.api.dto.inspectionSummary.InspectionSummaryDto;
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

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private com.weba11y.server.domain.enums.InspectionStatus status = com.weba11y.server.domain.enums.InspectionStatus.PENDING;

    @Builder.Default
    private Long totalViolations = 0L;

    @Builder.Default
    private Long completedViolations = 0L;

    @Builder.Default
    private Long pendingViolations = 0L;

    @Builder.Default
    private Long errorViolations = 0L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inspection_url_id")
    private InspectionUrl inspectionUrl;

    @OneToMany(mappedBy = "inspectionSummary", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 100)
    @Builder.Default
    private List<AccessibilityViolation> accessibilityViolations = new ArrayList<>();

    public void updateStatus(com.weba11y.server.domain.enums.InspectionStatus status) {
        this.status = status;
    }

    public void addViolation(AccessibilityViolation violation) {
        this.accessibilityViolations.add(violation);
        violation.setInspectionSummary(this);
    }

    public void recalculateViolations() {
        this.totalViolations = (long) this.accessibilityViolations.size();
        this.completedViolations = this.accessibilityViolations.stream().filter(v -> v.getStatus() == COMPLETED).count();
        this.pendingViolations = this.accessibilityViolations.stream().filter(v -> v.getStatus() == PENDING).count();
        this.errorViolations = this.accessibilityViolations.stream().filter(v -> v.getStatus() == ERROR).count();
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
        if (violations == null || violations.isEmpty()) {
            return InspectionSummaryDto.ViolationStatisticsDto.createEmpty();
        }
        long total = violations.size();
        long completed = violations.stream().filter(v -> v.getStatus() == COMPLETED).count();
        long pending = violations.stream().filter(v -> v.getStatus() == PENDING).count();
        long error = violations.stream().filter(v -> v.getStatus() == ERROR).count();

        Map<String, List<AccessibilityViolation>> byImportance = violations.stream()
                .collect(Collectors.groupingBy(v -> v.getInspectionItem().getImportance().name()));

        InspectionSummaryDto.ImportanceBreakdownDto importance = InspectionSummaryDto.ImportanceBreakdownDto.builder()
                .critical(toViolationCountDetail(byImportance.get("CRITICAL")))
                .serious(toViolationCountDetail(byImportance.get("SERIOUS")))
                .moderate(toViolationCountDetail(byImportance.get("MODERATE")))
                .minor(toViolationCountDetail(byImportance.get("MINOR")))
                .build();

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
        if (violations == null) return InspectionSummaryDto.ViolationCountDetail.createEmpty();
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
}