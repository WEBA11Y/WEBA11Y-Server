# Self-Invocation 문제 해결

## 개요

`AccessibilityCheckExecutor`, `StaticContentAccessibilityCheckerImpl`, `DynamicContentAccessibilityCheckerImpl`에서 `@Transactional` 메서드를 같은 클래스 내부에서 호출(Self-Invocation)하여 트랜잭션이 적용되지 않던 문제를 해결했습니다.

## 문제점

### Spring AOP 프록시 동작 원리

Spring의 `@Async`, `@Transactional` 등의 어노테이션은 **AOP 프록시**를 통해 동작합니다.

```
[외부 Bean] → [프록시] → [실제 객체.method()]   ← 어노테이션 동작함
[같은 클래스 내부] → this.method()               ← 어노테이션 무시됨
```

외부에서 호출하면 프록시를 거쳐 어노테이션이 동작하지만, 같은 클래스 내부에서 `this`를 통해 호출하면 프록시를 거치지 않아 **어노테이션이 완전히 무시**됩니다.

### 기존 코드

```java
// AccessibilityCheckExecutor.java
@Service
public class AccessibilityCheckExecutor {

    @Async("taskExecutor")
    public void runChecksAsync(...) {
        InspectionSummary summary = createAndPrepareInspectionSummary(...); // ⚠️ @Transactional 무시

        // ... CompletableFuture.thenRun() 콜백
        updateInspectionStatus(summary.getId(), InspectionStatus.COMPLETED); // ⚠️ @Transactional 무시
    }

    private void handleAsyncException(...) {
        updateInspectionStatus(summaryId, InspectionStatus.FAILED); // ⚠️ @Transactional 무시
    }

    @Transactional
    public InspectionSummary createAndPrepareInspectionSummary(...) { ... }

    @Transactional
    public void updateInspectionStatus(...) { ... }
}
```

```java
// StaticContentAccessibilityCheckerImpl.java / DynamicContentAccessibilityCheckerImpl.java
@Service
public class StaticContentAccessibilityCheckerImpl {

    @Override
    public CompletableFuture<Void> performCheck(...) {
        // ... 검사 로직 ...
        updateInspectionSummary(inspectionSummary, totalViolations); // ⚠️ @Transactional 무시
    }

    @Transactional
    public void updateInspectionSummary(...) { ... }
}
```

### 문제 분석

1. **트랜잭션 미적용**
   - `@Transactional` 메서드를 같은 클래스에서 호출하므로 트랜잭션이 동작하지 않음
   - 부분 실패 시 롤백이 불가능

2. **데이터 정합성 위험**
   - `InspectionSummary` 생성, 상태 업데이트, 위반사항 저장이 모두 트랜잭션 보호 없이 수행
   - 중간 실패 시 데이터가 불완전한 상태로 남을 수 있음

3. **영향 범위**
   - 총 5건의 Self-Invocation이 3개 클래스에 걸쳐 존재

### 발견된 문제 목록

| # | 클래스 | 호출 메서드 | 대상 메서드 | 어노테이션 | 심각도 |
|---|--------|------------|------------|-----------|--------|
| 1 | StaticContentAccessibilityCheckerImpl | performCheck() | updateInspectionSummary() | @Transactional | HIGH |
| 2 | DynamicContentAccessibilityCheckerImpl | performCheck() | updateInspectionSummary() | @Transactional | HIGH |
| 3 | AccessibilityCheckExecutor | runChecksAsync() | createAndPrepareInspectionSummary() | @Transactional | HIGH |
| 4 | AccessibilityCheckExecutor | runChecksAsync() | updateInspectionStatus() | @Transactional | HIGH |
| 5 | AccessibilityCheckExecutor | handleAsyncException() | updateInspectionStatus() | @Transactional | HIGH |

## 해결 방법

### 수정 방향

트랜잭션이 필요한 DB 작업을 별도의 `InspectionPersistenceService` 클래스로 분리하여, 외부 Bean 호출을 통해 프록시를 경유하도록 변경합니다.

### 수정된 구조

```java
// InspectionPersistenceService.java (신규)
@Service
@RequiredArgsConstructor
public class InspectionPersistenceService {

    @Transactional
    public InspectionSummary createAndPrepareInspectionSummary(...) { ... }

    @Transactional
    public void updateInspectionStatus(...) { ... }

    @Transactional
    public void updateInspectionSummary(...) { ... }
}
```

```java
// AccessibilityCheckExecutor.java (수정)
@Service
@RequiredArgsConstructor
public class AccessibilityCheckExecutor {

    private final InspectionPersistenceService inspectionPersistenceService; // 외부 Bean 주입

    @Async("taskExecutor")
    public void runChecksAsync(...) {
        InspectionSummary summary = inspectionPersistenceService.createAndPrepareInspectionSummary(...); // ✅ 프록시 통과
        // ...
        inspectionPersistenceService.updateInspectionStatus(...); // ✅ 프록시 통과
    }
}
```

```java
// StaticContentAccessibilityCheckerImpl.java (수정)
@Service
@RequiredArgsConstructor
public class StaticContentAccessibilityCheckerImpl {

    private final InspectionPersistenceService inspectionPersistenceService;

    @Override
    public CompletableFuture<Void> performCheck(...) {
        // ...
        inspectionPersistenceService.updateInspectionSummary(summary, totalViolations); // ✅ 프록시 통과
    }
}
```

### 핵심 변경사항

| 항목 | 변경 전 | 변경 후 |
|------|---------|---------|
| 트랜잭션 메서드 위치 | 각 클래스에 분산 | `InspectionPersistenceService`에 집중 |
| 메서드 호출 방식 | `this.method()` (Self-Invocation) | 외부 Bean 호출 (프록시 경유) |
| 트랜잭션 적용 | 무시됨 | 정상 동작 |
| 롤백 처리 | 불가능 | 정상 동작 |

## 효과

1. **트랜잭션 정상 동작**: 모든 DB 작업에 트랜잭션이 올바르게 적용되어 롤백 가능
2. **데이터 정합성 보장**: 부분 실패 시에도 데이터가 일관된 상태 유지
3. **책임 분리**: 검사 로직과 영속성 로직이 명확히 분리되어 유지보수 용이
4. **통합 관리**: 트랜잭션 관련 로직이 한 곳에 집중되어 관리 효율 향상

## 수정일

- 2026-02-10
