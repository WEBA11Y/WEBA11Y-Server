# AccessibilityViolation Bulk Insert 최적화

## 개요

`AccessibilityController.runChecks()` 실행 시 위반 항목(Violation) 저장 성능을 개선하기 위해,
기존 JPA `EntityManager.persist()` 방식에서 **JDBC Native Bulk Insert** 방식으로 전환하였다.

---

## 기존 문제점

### 1. IDENTITY 전략에 의한 Batch Insert 불가

```java
// AccessibilityViolation.java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
```

- `GenerationType.IDENTITY`는 DB의 `auto_increment`에 의존하므로, Hibernate가 INSERT 직후 생성된 ID를 즉시 알아야 한다.
- 이로 인해 **Hibernate는 JDBC batch insert를 수행하지 않으며**, `em.persist()` 호출마다 개별 INSERT 쿼리가 즉시 실행된다.

### 2. 기존 코드의 한계

```java
// 기존 AccessibilityViolationCustomRepositoryImpl.java
public void saveAll(List<AccessibilityViolation> violations) {
    for (int i = 0; i < violations.size(); i++) {
        em.persist(violations.get(i));        // 매번 개별 INSERT 실행
        if ((i + 1) % BATCH_SIZE == 0) {
            em.flush();
            em.clear();
        }
    }
    em.flush();
}
```

- 500건 단위로 `flush/clear`하여 메모리는 관리하지만, **쿼리 자체는 건건이 실행**된다.
- 1000건 저장 시 약 1000개의 INSERT 쿼리가 발생한다.

### 3. Hibernate Batch 설정 부재

`application.yml`에 `hibernate.jdbc.batch_size`, `hibernate.order_inserts` 등의 설정이 없었으나, IDENTITY 전략 사용 시에는 이 설정이 있어도 batch insert가 동작하지 않는다.

---

## Violation 저장 흐름

```
AccessibilityController.checkAccessibility()
  → AccessibilityCheckerServiceImpl.runChecks()
    → AccessibilityCheckExecutor.runChecksAsync()                    [@Async]
      ├→ StaticContentAccessibilityCheckerImpl.performCheck()        [@Async]
      └→ DynamicContentAccessibilityCheckerImpl.performCheck()       [@Async]
          ↓
      InspectionPersistenceServiceImpl.updateInspectionSummary()
        → accessibilityViolationRepository.saveAll(violations)       ← 저장 지점
          → AccessibilityViolationCustomRepositoryImpl.saveAll()     ← 실제 구현
```

- `InspectionPersistenceServiceImpl.updateInspectionSummary()`에서 DTO → Entity 변환 후 `saveAll()` 호출
- Static/Dynamic 체커가 각각 비동기로 실행되며, 각각의 결과를 `saveAll()`로 저장

---

## 적용한 해결 방법: JDBC Native Bulk Insert

### 변경 파일

| 파일 | 변경 내용 |
|------|----------|
| `AccessibilityViolationCustomRepositoryImpl.java` | `EntityManager` → `JdbcTemplate.batchUpdate()` |
| `application.yml` | HikariCP datasource에 bulk 옵션 추가 |

### 1. AccessibilityViolationCustomRepositoryImpl 변경

```java
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
                "(inspection_item, importance, assessment_level, description, code_line, " +
                "status, inspection_summary_id, create_date, update_date) " +
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
```

- JPA 영속성 컨텍스트를 우회하여 IDENTITY 전략의 제약을 받지 않음
- `JdbcTemplate.batchUpdate()`가 500건 단위로 JDBC batch 실행
- `saveAll()` 이후 violation 엔티티를 영속성 컨텍스트에서 재조회하지 않으므로 문제 없음

### 2. application.yml - MariaDB Bulk 옵션 추가

```yaml
datasource:
  hikari:
    data-source-properties:
      rewriteBatchedStatements: true
      useBulkStmts: true
```

| 옵션 | 설명 |
|------|------|
| `rewriteBatchedStatements` | JDBC batch INSERT를 하나의 multi-value INSERT 문으로 재작성 |
| `useBulkStmts` | MariaDB 전용 bulk statement 프로토콜 사용 |

이 설정으로 JDBC driver 레벨에서 개별 INSERT 500건이 다음과 같이 합쳐진다:

```sql
-- 변환 전: 500개의 개별 INSERT
INSERT INTO accessibility_violation (...) VALUES (...);
INSERT INTO accessibility_violation (...) VALUES (...);
...

-- 변환 후: 1개의 multi-value INSERT
INSERT INTO accessibility_violation (...) VALUES (...), (...), (...), ...;
```

---

## 성능 테스트 결과

H2 인메모리 DB 환경에서 `EntityManager.persist()` vs `JdbcTemplate.batchUpdate()` 실측 비교를 수행하였다.

테스트 클래스: `BulkInsertPerformanceTest.java`

### 측정 결과

| 데이터 건수 | EntityManager (기존) | JdbcTemplate (변경) | 성능 향상 |
|------------|---------------------|---------------------|----------|
| 100건 | 80 ms | 14 ms | **82.5% 감소** (5.7배) |
| 500건 | 188 ms | 10 ms | **94.7% 감소** (18.8배) |
| 1,000건 | 160 ms | 21 ms | **86.9% 감소** (7.6배) |
| 5,000건 | 560 ms | 84 ms | **85.0% 감소** (6.7배) |

### 분석

- 전체 구간에서 평균 **약 85~95% 성능 향상**을 확인하였다.
- 500건 구간에서 최대 **18.8배** 성능 차이가 발생하였다.
- 기존 방식은 데이터가 증가할수록 선형적으로 시간이 증가하지만, JdbcTemplate 방식은 batch 처리 덕분에 증가폭이 완만하다.
- 실제 MariaDB 환경에서는 `rewriteBatchedStatements=true` 옵션으로 multi-value INSERT가 적용되므로, 네트워크 라운드트립 감소로 인해 성능 차이가 더 크게 나타날 수 있다.

> **참고**: H2 인메모리 DB는 네트워크 오버헤드가 없으므로, 실제 MariaDB 원격 DB 환경에서는 개선 효과가 더 클 것으로 예상된다.

---

## 고려사항

- **영속성 컨텍스트 우회**: `JdbcTemplate`은 JPA 영속성 컨텍스트를 거치지 않으므로, `saveAll()` 이후 해당 엔티티를 1차 캐시에서 조회할 수 없다. 현재 코드에서는 저장 후 violation을 재조회하지 않으므로 영향 없음.
- **BaseEntity 타임스탬프**: JPA `@PrePersist`가 동작하지 않으므로, SQL에서 직접 `create_date`, `update_date`를 설정한다.
- **BATCH_SIZE 조정**: 현재 500으로 설정되어 있으며, 데이터 크기와 DB 환경에 따라 조정 가능하다.
