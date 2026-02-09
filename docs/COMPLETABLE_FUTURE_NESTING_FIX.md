# CompletableFuture 중첩 문제 해결

## 개요

`DynamicContentAccessibilityCheckerImpl.java`에서 `@Async`와 `CompletableFuture.runAsync()`가 불필요하게 중첩 사용되던 문제를 해결했습니다.

## 문제점

### 기존 코드

```java
@Override
@Async("taskExecutor")
public CompletableFuture<Void> performCheck(Page page, SseEmitter emitter, InspectionSummary summary) {
    log.info("[DynamicCheckerImpl] Starting dynamic accessibility checks for inspection: {}", summary.getId());
    return CompletableFuture.runAsync(() -> {  // 불필요한 중첩
        try {
            // ... 검사 로직 ...
        } catch (Exception e) {
            throw new CompletionException(e);
        }
    });
}
```

### 문제 분석

1. **이중 스레드 전환 발생**
   - `@Async("taskExecutor")`: `taskExecutor` 스레드 풀에서 메서드 실행
   - `CompletableFuture.runAsync()`: `ForkJoinPool.commonPool()`에서 추가 실행

2. **리소스 낭비**
   - 동일 작업에 두 개의 스레드 풀 사용
   - 불필요한 컨텍스트 스위칭 오버헤드

3. **스레드 풀 관리 분산**
   - `taskExecutor`로 통합 관리하려던 의도와 달리 `commonPool` 사용
   - 스레드 모니터링 및 제어 어려움

## 해결 방법

### 수정된 코드

```java
@Override
@Async("taskExecutor")
public CompletableFuture<Void> performCheck(Page page, SseEmitter emitter, InspectionSummary summary) {
    log.info("[DynamicCheckerImpl] Starting dynamic accessibility checks for inspection: {}", summary.getId());

    try {
        List<AccessibilityViolationDto> totalViolations = new ArrayList<>();

        for (DynamicChecker checker : dynamicCheckers) {
            if (page.isClosed()) {
                log.warn("Page was closed, stopping dynamic checks for inspection: {}", summary.getId());
                break;
            }
            try {
                List<AccessibilityViolationDto> violations = checker.checkDynamic(page, summary.getId());
                violations.forEach(v -> sseEventSender.sendViolationEvent(emitter, v));
                totalViolations.addAll(violations);
            } catch (Exception e) {
                log.error("[DynamicCheckerImpl] Error in checker '{}' for inspection: {}. Skipping checker.",
                        checker.getClass().getSimpleName(), summary.getId(), e);
                sseEventSender.sendErrorEvent(emitter, "Error in checker " + checker.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }

        inspectionPersistenceService.updateInspectionSummary(summary, totalViolations);

        log.info("[DynamicCheckerImpl] Finished all dynamic checks for inspection: {}. Total violations: {}",
                summary.getId(), totalViolations.size());

        return CompletableFuture.completedFuture(null);  // 성공 시

    } catch (Exception e) {
        log.error("[DynamicCheckerImpl] Unrecoverable error during dynamic check process for inspection: {}", summary.getId(), e);
        return CompletableFuture.failedFuture(e);  // 실패 시
    }
}
```

### 핵심 변경사항

| 항목 | 변경 전 | 변경 후 |
|------|---------|---------|
| 비동기 실행 | `@Async` + `runAsync()` | `@Async`만 사용 |
| 스레드 풀 | `taskExecutor` + `commonPool` | `taskExecutor`만 사용 |
| 반환 방식 | `runAsync()` 결과 반환 | `completedFuture()` / `failedFuture()` |
| 예외 처리 | `CompletionException` 래핑 | 직접 `failedFuture()` 반환 |

## 추가 개선 사항

### 미사용 메서드 제거

기존에 정의되어 있었지만 사용되지 않던 `handleException` 메서드를 제거했습니다.

```java
// 제거된 코드
private void handleException(SseEmitter emitter, Throwable ex, Long inspectionId) {
    log.error("[DynamicCheckerImpl] Error during dynamic accessibility check: {}", inspectionId, ex);
    sseEventSender.sendErrorEvent(emitter, "Dynamic check failed: " + ex.getMessage());
    emitter.completeWithError(ex);
}
```

## 효과

1. **성능 향상**: 불필요한 스레드 전환 제거로 오버헤드 감소
2. **리소스 효율성**: 단일 스레드 풀(`taskExecutor`)로 통합 관리
3. **코드 간결성**: 불필요한 중첩 제거로 가독성 향상
4. **디버깅 용이**: 스레드 추적 및 모니터링 단순화

## 수정일

- 2026-02-09
