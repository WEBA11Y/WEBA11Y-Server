package com.weba11y.server.dto.inspectionSummary;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class InspectionSummaryDto {
    private Long id;
    private Long inspectionUrlId;
    private ViolationStatisticsDto violations;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createDate;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateDate;

    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    public static class ViolationStatisticsDto {
        private long totalViolations;
        private long completedViolations;
        private long pendingViolations;
        private long errorViolations;
        private ImportanceBreakdownDto importance;
        private AssessmentLevelBreakdownDto assessmentLevel;
    }

    // 중요도(Importance)별 통계를 위한 DTO
    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    public static class ImportanceBreakdownDto {
        private ViolationCountDetail critical;
        private ViolationCountDetail serious;
        private ViolationCountDetail moderate;
        private ViolationCountDetail minor;
    }

    // 평가 레벨(AssessmentLevel)별 통계를 위한 DTO
    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    public static class AssessmentLevelBreakdownDto {
        private ViolationCountDetail A;
        private ViolationCountDetail AA;
        private ViolationCountDetail AAA;
        private ViolationCountDetail BEST;
    }

    // 개별 통계 (total, completed, pending, error)를 위한 공통 DTO
    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    public static class ViolationCountDetail {
        private long total;
        private long completed;
        private long pending;
        private long error;
    }

    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    public static class InspectionSummaryMetadataDto {
        private Long id;
        private LocalDateTime createDate;
        private LocalDateTime updateDate;
    }



}
