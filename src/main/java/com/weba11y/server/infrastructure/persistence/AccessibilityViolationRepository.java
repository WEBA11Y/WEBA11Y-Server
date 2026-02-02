package com.weba11y.server.infrastructure.persistence;

import com.weba11y.server.domain.violation.AccessibilityViolation;
import com.weba11y.server.domain.enums.InspectionItems;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository

public interface AccessibilityViolationRepository extends JpaRepository<AccessibilityViolation, Long>, AccessibilityViolationCustomRepository {
    @Query(value = "SELECT av FROM AccessibilityViolation av " +
            "WHERE av.inspectionSummary.id = :inspectionSummaryId ")
    List<AccessibilityViolation> findByInspectionSummaryId(@Param("inspectionSummaryId") Long inspectionSummaryId);

    @Query(value = "SELECT av FROM AccessibilityViolation av " +
            "WHERE av.inspectionSummary.id = :inspectionSummaryId " +
            "AND av.inspectionItem IN :items " +
            "ORDER BY av.inspectionItem")
    Page<AccessibilityViolation> findByInspectionSummaryIdAndItems(Pageable pageable,
                                                                   @Param("inspectionSummaryId") Long inspectionSummaryId,
                                                                   @Param("items") List<InspectionItems> items);

    @Query("SELECT av " +
            "FROM AccessibilityViolation av " +
            "WHERE av.inspectionSummary.id = :summaryId " +
            "ORDER BY av.importance ASC, " +
            "av.assessmentLevel ASC " +
            "LIMIT 5")
    List<AccessibilityViolation> findTop5BySummaryIdOrderByPriority(@Param("summaryId") Long inspectionSummaryId);
}