# InspectionSummary Race Condition 해결

## 개요

`StaticContentAccessibilityCheckerImpl`과 `DynamicContentAccessibilityCheckerImpl`이 병렬로 실행되면서 동일한 `InspectionSummary` 엔티티를 동시에 업데이트하여 데이터가 유실되던 Race Condition 문제를 해결했습니다.

## 문제점

### 기존 코드

```java
// AccessibilityCheckExecutor.java
@Async("taskExecutor")
public void runChecksAsync(InspectionUrlDto inspectionUrl, SseEmitter emitter) {
    InspectionSummary summary = inspectionPersistenceService.createAndPrepareInspectionSummary(inspectionUrl.getId());

    // 두 checker가 동일한 summary 엔티티를 받아 병렬 실행
    CompletableFuture<Void> staticCheckFuture = staticContentAccessibilityChecker.performCheck(document, emitter, summary);
    CompletableFuture<Void> dynamicCheckFuture = dynamicContentAccessibilityChecker.performCheck(loadedPage, emitter, summary);

    CompletableFuture.allOf(staticCheckFuture, dynamicCheckFuture)
            .whenComplete((result, ex) -> {
                // 이 시점에서는 이미 각 checker가 개별적으로 summary를 업데이트한 상태
                inspectionPersistenceService.updateInspectionStatus(summary.getId(), InspectionStatus.COMPLETED);
            });
}
```

```java
// StaticContentAccessibilityCheckerImpl.java
@Async("taskExecutor")
public CompletableFuture<Void> performCheck(Document document, SseEmitter emitter, InspectionSummary inspectionSummary) {
    // ... 검사 수행 ...
    inspectionPersistenceService.updateInspectionSummary(inspectionSummary, totalViolations); // ⚠️ 동시 업데이트 1
}
```

```java
// DynamicContentAccessibilityCheckerImpl.java
@Async("taskExecutor")
public CompletableFuture<Void> performCheck(Page page, SseEmitter emitter, InspectionSummary summary) {
    // ... 검사 수행 ...
    inspectionPersistenceService.updateInspectionSummary(summary, totalViolations); // ⚠️ 동시 업데이트 2
}
```

```java
// InspectionPersistenceServiceImpl.java
@Transactional
public void updateInspectionSummary(InspectionSummary inspectionSummary, List<AccessibilityViolationDto> totalViolations) {
    List<AccessibilityViolation> violations = totalViolations.stream()
            .map(dto -> dto.toEntity(inspectionSummary))
            .collect(Collectors.toList());
    accessibilityViolationRepository.saveAll(violations);

    inspectionSummary.recalculateViolations(); // ⚠️ detached 엔티티의 in-memory 리스트 기반 계산
    summaryRepository.save(inspectionSummary); // ⚠️ 마지막 save가 이전 save를 덮어씀
}
```

### 문제 분석

```
Thread A (Static)                  Thread B (Dynamic)
─────────────────                  ──────────────────
violations A 저장
recalculate (A만 반영)
summary.save()                     violations B 저장
                                   recalculate (B만 반영)
                                   summary.save()  ← Thread A의 결과 덮어씀
```

1. **Last Writer Wins (데이터 유실)**
   - 두 스레드가 동일한 detached `InspectionSummary` 엔티티를 공유
   - 각각 `recalculateViolations()` 호출 시 자신의 violation만 반영
   - 마지막에 save하는 스레드가 먼저 save한 스레드의 violation 카운트를 덮어씀

2. **Detached 엔티티 공유**
   - `createAndPrepareInspectionSummary()`의 트랜잭션이 종료되면서 엔티티가 detach됨
   - 두 스레드가 동일한 detached 객체 참조를 가지고 각각 다른 트랜잭션에서 save

3. **Violation 카운트 불일치**
   - `recalculateViolations()`는 in-memory `accessibilityViolations` 리스트 기준으로 계산
   - `saveAll()`로 직접 저장한 violation은 이 리스트에 반영되지 않음
   - 결과적으로 totalViolations, pendingViolations 등의 카운트가 실제 DB 데이터와 불일치

## 해결 방법

### 수정 방향

락 메커니즘을 도입하는 대신, **동시 업데이트 자체가 발생하지 않도록 구조를 변경**합니다. Checker는 violation 목록만 반환하고, Executor에서 두 결과를 합친 후 단일 지점에서 summary를 업데이트합니다.

### 수정된 구조

```
[변경 전]
Executor → Static Checker  ──(각자 updateInspectionSummary)──→ DB
         → Dynamic Checker ──(각자 updateInspectionSummary)──→ DB

[변경 후]
Executor → Static Checker  ──(violations 반환)──┐
         → Dynamic Checker ──(violations 반환)──┤
                                                 └→ Executor에서 합산 후 1회 updateInspectionSummary → DB
```

```java
// StaticContentAccessibilityChecker.java (인터페이스 변경)
public interface StaticContentAccessibilityChecker {
    CompletableFuture<List<AccessibilityViolationDto>> performCheck(Document document, SseEmitter emitter, Long summaryId);
}
```

```java
// DynamicContentAccessibilityChecker.java (인터페이스 변경)
public interface DynamicContentAccessibilityChecker {
    CompletableFuture<List<AccessibilityViolationDto>> performCheck(Page page, SseEmitter emitter, Long summaryId);
}
```

```java
// AccessibilityCheckExecutor.java (결과 수집 및 단일 업데이트)
CompletableFuture<List<AccessibilityViolationDto>> staticCheckFuture =
        staticContentAccessibilityChecker.performCheck(document, emitter, summary.getId());
CompletableFuture<List<AccessibilityViolationDto>> dynamicCheckFuture =
        dynamicContentAccessibilityChecker.performCheck(loadedPage, emitter, summary.getId());

staticCheckFuture.thenCombine(dynamicCheckFuture, (staticViolations, dynamicViolations) -> {
            List<AccessibilityViolationDto> allViolations = new ArrayList<>(staticViolations.size() + dynamicViolations.size());
            allViolations.addAll(staticViolations);
            allViolations.addAll(dynamicViolations);
            return allViolations;
        })
        .whenComplete((allViolations, ex) -> {
            pageLoaderService.closePage(loadedPage);
            if (ex != null) {
                handleAsyncException(emitter, summary.getId(), ex);
            } else {
                inspectionPersistenceService.updateInspectionSummary(summary.getId(), allViolations);
                inspectionPersistenceService.updateInspectionStatus(summary.getId(), InspectionStatus.COMPLETED);
                sseEventSender.send(emitter, "complete", "All accessibility checks completed.");
                emitter.complete();
            }
        });
```

```java
// InspectionPersistenceServiceImpl.java (summaryId로 fresh 엔티티 조회)
@Transactional
public void updateInspectionSummary(Long summaryId, List<AccessibilityViolationDto> totalViolations) {
    InspectionSummary summary = summaryRepository.findById(summaryId).orElseThrow(
            () -> new NoSuchElementException("InspectionSummary Not Found: " + summaryId)
    );
    List<AccessibilityViolation> violations = totalViolations.stream()
            .map(dto -> dto.toEntity(summary))
            .collect(Collectors.toList());
    accessibilityViolationRepository.saveAll(violations);

    summary.recalculateViolations();
    summaryRepository.save(summary);
}
```

### 핵심 변경사항

| 항목 | 변경 전 | 변경 후 |
|------|---------|---------|
| Checker 반환 타입 | `CompletableFuture<Void>` | `CompletableFuture<List<AccessibilityViolationDto>>` |
| Checker 파라미터 | `InspectionSummary` 엔티티 | `Long summaryId` |
| Checker 책임 | 검사 + 영속성 업데이트 | 검사만 수행 |
| Summary 업데이트 지점 | Static/Dynamic Checker 각각 (2회) | Executor에서 합산 후 (1회) |
| Summary 조회 방식 | detached 엔티티 재사용 | `summaryId`로 fresh 엔티티 조회 |
| Future 합성 방식 | `CompletableFuture.allOf()` | `thenCombine()`으로 결과 수집 |

### 수정 파일 목록

| # | 파일 | 변경 내용 |
|---|------|----------|
| 1 | `StaticContentAccessibilityChecker.java` | 반환 타입 변경, 파라미터 `summaryId`로 변경 |
| 2 | `DynamicContentAccessibilityChecker.java` | 반환 타입 변경, 파라미터 `summaryId`로 변경 |
| 3 | `StaticContentAccessibilityCheckerImpl.java` | `InspectionPersistenceService` 의존성 제거, violation 목록 반환 |
| 4 | `DynamicContentAccessibilityCheckerImpl.java` | `InspectionPersistenceService` 의존성 제거, violation 목록 반환 |
| 5 | `AccessibilityCheckExecutor.java` | `thenCombine`으로 결과 합산 후 단일 업데이트 |
| 6 | `InspectionPersistenceService.java` | `updateInspectionSummary` 시그니처 변경 |
| 7 | `InspectionPersistenceServiceImpl.java` | `summaryId`로 fresh 엔티티 조회 후 업데이트 |

## 효과

1. **Race Condition 제거**: Summary 업데이트가 단일 지점에서 1회만 수행되어 동시 업데이트 불가
2. **데이터 정합성 보장**: 모든 violation이 합산된 후 한 번에 저장되므로 카운트 불일치 해소
3. **책임 분리 강화**: Checker는 순수하게 검사만 수행, 영속성 로직은 Executor/PersistenceService에 집중
4. **Detached 엔티티 문제 해소**: `summaryId`로 트랜잭션 내에서 fresh 엔티티를 조회하여 사용

## 수정일

- 2026-02-10
