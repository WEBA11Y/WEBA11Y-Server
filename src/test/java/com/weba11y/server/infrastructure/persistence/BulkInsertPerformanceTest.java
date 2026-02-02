package com.weba11y.server.infrastructure.persistence;

import com.weba11y.server.domain.enums.InspectionItems;
import com.weba11y.server.domain.enums.InspectionStatus;
import com.weba11y.server.domain.inspection.summary.InspectionSummary;
import com.weba11y.server.domain.inspection.url.InspectionUrl;
import com.weba11y.server.domain.member.Member;
import com.weba11y.server.domain.violation.AccessibilityViolation;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@ActiveProfiles("test")
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BulkInsertPerformanceTest {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private InspectionUrlRepository inspectionUrlRepository;

    @Autowired
    private InspectionSummaryRepository summaryRepository;

    private static final int[] TEST_SIZES = {100, 500, 1000, 5000};
    private static final int BATCH_SIZE = 500;
    private static final InspectionItems[] ITEMS = InspectionItems.values();

    private static final List<String> results = new ArrayList<>();
    private static int counter = 0;

    private InspectionSummary createSummary() {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        return tx.execute(status -> {
            int seq = ++counter;
            Member member = Member.builder()
                    .userId("pf" + String.format("%06d", seq))
                    .password("password")
                    .name("tester")
                    .phoneNum("010" + String.format("%08d", seq))
                    .birthday(LocalDate.of(2000, 1, 1))
                    .build();
            memberRepository.save(member);

            InspectionUrl url = InspectionUrl.builder()
                    .description("Performance Test URL")
                    .url("https://example.com")
                    .member(member)
                    .build();
            inspectionUrlRepository.save(url);

            InspectionSummary summary = InspectionSummary.builder()
                    .inspectionUrl(url)
                    .status(InspectionStatus.IN_PROGRESS)
                    .build();
            return summaryRepository.save(summary);
        });
    }

    private List<AccessibilityViolation> createViolations(int count, InspectionSummary summary) {
        List<AccessibilityViolation> violations = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            violations.add(AccessibilityViolation.builder()
                    .inspectionItem(ITEMS[i % ITEMS.length])
                    .description("Test violation #" + i)
                    .codeLine("<div>test code line " + i + "</div>")
                    .inspectionSummary(summary)
                    .build());
        }
        return violations;
    }

    /**
     * 기존 방식: EntityManager.persist() - 건건이 INSERT
     */
    @Test
    @Order(1)
    @DisplayName("[기존] EntityManager.persist() 성능 측정")
    void testEntityManagerPersist() {
        System.out.println("\n========================================");
        System.out.println(" EntityManager.persist() (기존 방식)");
        System.out.println("========================================");

        for (int size : TEST_SIZES) {
            InspectionSummary summary = createSummary();
            List<AccessibilityViolation> violations = createViolations(size, summary);

            TransactionTemplate tx = new TransactionTemplate(transactionManager);
            long elapsed = tx.execute(status -> {
                long start = System.currentTimeMillis();
                for (int i = 0; i < violations.size(); i++) {
                    em.persist(violations.get(i));
                    if ((i + 1) % BATCH_SIZE == 0) {
                        em.flush();
                        em.clear();
                    }
                }
                em.flush();
                em.clear();
                return System.currentTimeMillis() - start;
            });

            String result = String.format("[EntityManager] %5d건 → %d ms", size, elapsed);
            System.out.println(result);
            results.add(result);
        }
    }

    /**
     * 변경 방식: JdbcTemplate.batchUpdate() - Bulk INSERT
     */
    @Test
    @Order(2)
    @DisplayName("[변경] JdbcTemplate.batchUpdate() 성능 측정")
    void testJdbcTemplateBatchUpdate() {
        System.out.println("\n========================================");
        System.out.println(" JdbcTemplate.batchUpdate() (변경 방식)");
        System.out.println("========================================");

        String sql = "INSERT INTO accessibility_violation " +
                "(inspection_item, importance, assessment_level, description, code_line, status, inspection_summary_id, create_date, update_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        for (int size : TEST_SIZES) {
            InspectionSummary summary = createSummary();
            List<AccessibilityViolation> violations = createViolations(size, summary);

            TransactionTemplate tx = new TransactionTemplate(transactionManager);
            long elapsed = tx.execute(status -> {
                LocalDateTime now = LocalDateTime.now();
                long start = System.currentTimeMillis();
                jdbcTemplate.batchUpdate(sql, violations, BATCH_SIZE, (ps, v) -> {
                    ps.setString(1, v.getInspectionItem().name());
                    ps.setInt(2, v.getImportance());
                    ps.setInt(3, v.getAssessmentLevel());
                    ps.setString(4, v.getDescription());
                    ps.setString(5, v.getCodeLine());
                    ps.setString(6, v.getStatus().name());
                    ps.setLong(7, v.getInspectionSummary().getId());
                    ps.setTimestamp(8, Timestamp.valueOf(now));
                    ps.setTimestamp(9, Timestamp.valueOf(now));
                });
                return System.currentTimeMillis() - start;
            });

            String result = String.format("[JdbcTemplate] %5d건 → %d ms", size, elapsed);
            System.out.println(result);
            results.add(result);
        }
    }

    @Test
    @Order(3)
    @DisplayName("성능 비교 결과 요약")
    void printSummary() {
        System.out.println("\n========================================");
        System.out.println(" 성능 비교 결과 요약");
        System.out.println("========================================");
        for (String r : results) {
            System.out.println(r);
        }
        System.out.println("========================================\n");
    }
}
