package com.weba11y.server.infrastructure.persistence;

import com.weba11y.server.domain.violation.AccessibilityViolation;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class AccessibilityViolationCustomRepositoryImpl implements AccessibilityViolationCustomRepository {
    private final JdbcTemplate jdbcTemplate;
    private static final int BATCH_SIZE = 500;

    @Override
    public void saveAll(List<AccessibilityViolation> violations) {
        if (violations.isEmpty()) {
            return;
        }

        String sql = "INSERT INTO accessibility_violation " +
                "(inspection_item, importance, assessment_level, description, code_line, status, inspection_summary_id, create_date, update_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        LocalDateTime now = LocalDateTime.now();

        jdbcTemplate.batchUpdate(sql, violations, BATCH_SIZE, (ps, violation) -> {
            ps.setString(1, violation.getInspectionItem().name());
            ps.setInt(2, violation.getImportance());
            ps.setInt(3, violation.getAssessmentLevel());
            ps.setString(4, violation.getDescription());
            ps.setString(5, violation.getCodeLine());
            ps.setString(6, violation.getStatus().name());
            ps.setLong(7, violation.getInspectionSummary().getId());
            ps.setTimestamp(8, Timestamp.valueOf(now));
            ps.setTimestamp(9, Timestamp.valueOf(now));
        });
    }
}
