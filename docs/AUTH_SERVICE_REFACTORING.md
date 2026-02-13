# AuthService 점검 결과

## 1. 버그 수정 (완료)

### 1-1. Refresh Token 쿠키명 불일치 (NPE 발생)

**문제**

| 파일 | 값 |
|------|-----|
| `CookieName.java:4` | `"refresh_token"` |
| `AuthController.java:30` | `"refresh_token"` (중복 상수) |
| `GlobalExceptionHandler.java:66` | `"refresh_token_key"` **(불일치)** |

`handleExpiredRefreshTokenException`에서 `"refresh_token_key"`로 쿠키를 찾기 때문에 항상 `null`이 반환되고,
`CookieUtil.deleteCookie(response, null)`에서 **NullPointerException이 발생**한다.

**수정 내용**

| 파일 | 변경 사항 |
|------|-----------|
| `GlobalExceptionHandler.java` | 쿠키명을 `CookieName.REFRESH_TOKEN_COOKIE` 상수로 통일, `null` 체크 추가 |
| `AuthController.java` | 중복 상수 `REFRESH_TOKEN_COOKIE` 제거, `CookieName.REFRESH_TOKEN_COOKIE` import로 통일 |

```java
// Before - GlobalExceptionHandler.java
Cookie refreshTokenCookie = CookieUtil.findCookie(request, "refresh_token_key");
CookieUtil.deleteCookie(response, refreshTokenCookie); // NPE 발생

// After
Cookie refreshTokenCookie = CookieUtil.findCookie(request, REFRESH_TOKEN_COOKIE);
if (refreshTokenCookie != null) {
    CookieUtil.deleteCookie(response, refreshTokenCookie);
}
```

```java
// Before - AuthController.java
private static final String REFRESH_TOKEN_COOKIE = "refresh_token"; // 중복 상수

// After - 제거하고 CookieName.REFRESH_TOKEN_COOKIE를 static import
import static com.weba11y.server.infrastructure.security.CookieName.REFRESH_TOKEN_COOKIE;
```

---

### 1-2. `tokenIsExpired()` 메서드 의미 반전

**문제**

- 토큰이 **유효하면** `true`, **만료되면** `false`를 반환하여 메서드명과 **의미가 반대**
- `getTokenInfo()`에서 호출하지만 **반환값을 무시**하고 있어, 만료된 토큰으로도 처리가 진행됨

**수정 내용**

| 파일 | 변경 사항 |
|------|-----------|
| `AuthService.java` | `tokenIsExpired(String)` -> `isTokenValid(String)` 메서드명 변경 |
| `AuthServiceImpl.java` | 메서드명 변경 + `getTokenInfo()`에서 반환값 검증 후 만료 시 예외 발생 |

```java
// Before - AuthServiceImpl.java
public TokenInfo getTokenInfo(String token) {
    tokenIsExpired(token); // 반환값 무시
    return JwtUtil.getTokenInfo(token, secret);
}
public boolean tokenIsExpired(String token) {
    try {
        JwtUtil.validateToken(token, secret);
        return true;  // 유효한데 true(만료됨)를 반환
    } catch (ExpiredTokenException e) {
        return false; // 만료됐는데 false(만료 안 됨)를 반환
    }
}

// After
public TokenInfo getTokenInfo(String token) {
    if (!isTokenValid(token)) {
        throw new ExpiredTokenException("토큰이 만료되었습니다.");
    }
    return JwtUtil.getTokenInfo(token, secret);
}
public boolean isTokenValid(String token) {
    try {
        JwtUtil.validateToken(token, secret);
        return true;  // 유효하면 true
    } catch (ExpiredTokenException e) {
        return false; // 만료되면 false
    }
}
```

---

### 1-3. `ShouldNotFilterPath` 글로브 패턴 미작동

**문제**

- `"/swagger-ui/**"`, `"/api/v1/join/**"` 등 글로브 패턴이 포함되어 있으나, `JwtFilter`에서 `String.startsWith()`로 비교
- `"/swagger-ui/index.html".startsWith("/swagger-ui/**")` = `false` → Swagger UI 경로가 필터 제외에서 **누락**

**수정 내용**

| 파일 | 변경 사항 |
|------|-----------|
| `ShouldNotFilterPath.java` | 글로브 패턴(`/**`) 제거, `startsWith` 비교와 호환되도록 prefix만 유지 |

```java
// Before
public static final List<String> EXCLUDE_PATHS = Arrays.asList(
    "/v1/api-docs",
    "/swagger-ui/**",      // startsWith 비교 시 매칭 실패
    "/api/v1/join",
    "/api/v1/login",
    "/api/v1/join/**",     // "/api/v1/join"과 중복
    "/api/v1/reissuing-token"
);

// After
public static final List<String> EXCLUDE_PATHS = Arrays.asList(
    "/v1/api-docs",
    "/swagger-ui",         // startsWith로 하위 경로 모두 매칭
    "/api/v1/join",        // startsWith로 /join/check-userId 등도 매칭
    "/api/v1/login",
    "/api/v1/reissuing-token"
);
```

---

### 1-4. `join()` 예외 메시지 포맷 오류

**문제**

- `{}`는 SLF4J 플레이스홀더이지 String 포맷이 아니므로, 실제 예외 정보가 메시지에 포함되지 않음
- 원인 예외(cause)도 전달되지 않아 스택 트레이스 추적 불가

**수정 내용**

| 파일 | 변경 사항 |
|------|-----------|
| `AuthServiceImpl.java` | 예외 메시지에 `e.getMessage()` 연결 + 원인 예외 `e`를 cause로 전달 |

```java
// Before
throw new RuntimeException("회원가입 중 오류발생 {}");

// After
throw new RuntimeException("회원가입 중 오류발생: " + e.getMessage(), e);
```

---

## 2. 보안 취약점

### 2-1. Cookie에 `Secure` / `SameSite` 플래그 미설정 — **수정 완료**

- `CookieUtil.java` — `HttpOnly`만 설정
- `Secure` 미설정 시 HTTP로 토큰 전송 가능 (MITM 공격 노출)
- `SameSite` 미설정 시 CSRF 공격에 취약 (특히 `/api/v1/reissuing-token`)

**수정 내용**

| 파일 | 변경 사항 |
|------|-----------|
| `CookieUtil.java` | `addCookie()`, `deleteCookie()`에 `Secure=true`, `SameSite=Strict` 플래그 추가 |

### 2-2. 비활성화된 회원 로그인 가능 — **수정 완료**

- `AuthServiceImpl.login()` — `MemberStatus` 검증 없음
- `DEACTIVATED` 상태의 회원도 정상 로그인되어 토큰 발급

**수정 내용**

| 파일 | 변경 사항 |
|------|-----------|
| `AuthServiceImpl.java` | `login()`에 `MemberStatus.DEACTIVATED` 검증 추가, `DeactivatedMemberException` 발생 |
| `DeactivatedMemberException.java` | 신규 커스텀 예외 클래스 생성 |
| `GlobalExceptionHandler.java` | `DeactivatedMemberException` 핸들러 추가 (HTTP 403) |
| `Member.java` | `activate()` 메서드 추가 |
| `AuthService.java` | `deactivateMember()`, `activateMember()` 인터페이스 추가 |
| `AuthServiceImpl.java` | `deactivateMember()`, `activateMember()` 구현 |
| `AuthController.java` | `PATCH /api/v1/member/deactivate`, `PATCH /api/v1/member/activate` API 추가 |

### 2-3. Refresh Token 재사용 공격 취약 — **수정 완료**

- 토큰 재발급 시 기존 Refresh Token이 그대로 유효
- Refresh Token이 탈취되면 만료 시점(14일)까지 무제한 악용 가능
- **Refresh Token Rotation** 미적용

**수정 내용**

| 파일 | 변경 사항 |
|------|-----------|
| `AuthService.java` | `reissuingAccessToken()` 시그니처에 `HttpServletResponse` 추가 |
| `AuthServiceImpl.java` | Access Token 재발급 시 새로운 Refresh Token도 함께 발급하여 쿠키 갱신 (Refresh Token Rotation) |
| `AuthController.java` | `reissuingAccessToken()`에 `HttpServletResponse` 전달 |

### 2-4. 로그아웃 시 토큰 무효화 미흡 (미수정)

- `AuthController.logout()` — 쿠키만 삭제하고 서버 측 토큰 무효화 없음
- 토큰 값을 탈취한 공격자는 로그아웃 후에도 사용 가능
- Redis 기반 토큰 블랙리스트 구현 필요 (추후 구현 권장)

### 2-5. 로그인 브루트포스 방어 부재 — **수정 완료**

- `AccountLockedException` 핸들러는 있으나, 실제 로그인 실패 횟수 추적/계정 잠금 로직 없음

**수정 내용**

| 파일 | 변경 사항 |
|------|-----------|
| `LoginAttemptService.java` | Redis 기반 로그인 실패 횟수 추적 서비스 신규 생성 (5회 실패 시 30분 차단) |
| `AuthServiceImpl.java` | `login()`에서 `LoginAttemptService` 연동 — 차단 여부 확인, 실패 시 카운트 증가, 성공 시 초기화 |

### 2-6. 비밀번호 패턴 미검증 — **수정 완료**

- `JoinDto` / `LoginDto` — `@Size`만 검증
- 메시지에는 "대소문자 영문, 숫자, 특수기호 조합"이라고 안내하지만, `@Pattern`으로 실제 패턴을 강제하지 않음

**수정 내용**

| 파일 | 변경 사항 |
|------|-----------|
| `JoinDto.java` | `password`에 `@Pattern` 추가 (대문자, 소문자, 숫자, 특수기호 각 1개 이상 필수) |
| `JoinDto.java` | `userId`에 `@Pattern` 추가 (영문, 숫자만 허용) |

### 2-7. jjwt Deprecated API 사용 (미수정)

- `JwtUtil.java` — `Jwts.parser().setSigningKey(String)` 사용
- String 기반 키는 보안상 취약하며, `Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(...)).build()` 사용 권장
- jjwt 라이브러리 버전 업그레이드(0.9.1 -> 0.12.x) 필요 (추후 구현 권장)

---

## 3. 설계 문제 (미수정)

### 3-1. AuthService의 SRP(단일 책임 원칙) 위반

`AuthService` 인터페이스가 **인증**과 **회원 관리**를 모두 담당하고 있다.

```
AuthService
+-- 인증: login, reissuingAccessToken, getTokenInfo, isTokenValid
+-- 회원: retrieveMember, updateMember, deleteMember, isExistsUserId, isExistsPhoneNum
```

`MemberService`를 분리하는 것이 바람직하다.

### 3-2. `JwtUtil`이 `@Component`이면서 모든 메서드가 `static`

- `@Component`로 등록되어 있으나 모든 메서드가 static
- `@Component`를 제거하거나, 인스턴스 메서드로 변경하여 `secret` 등을 필드로 주입하는 방식이 적절하다.

### 3-3. `CurrentMemberIdArgumentResolver`에서 null 반환

- 인증 실패 시 `null` 반환
- 컨트롤러에서 `@CurrentMemberId Long memberId`가 `null`이 되어 런타임에 예기치 않은 오류 발생 가능
- 예외를 던지는 것이 안전하다.

### 3-4. `deleteMember()`의 에러 처리

- 실패 시 예외 대신 `"회원 탈퇴 실패"` 문자열을 HTTP 200으로 반환
- 클라이언트가 성공/실패를 구분할 수 없다.

---

## 4. 우선순위 정리

| 우선순위 | 항목 | 분류 | 상태 |
|---------|------|------|------|
| **P0** | 쿠키명 불일치 (NPE) | 버그 | **수정 완료** |
| **P0** | `tokenIsExpired()` 의미 반전 | 버그 | **수정 완료** |
| **P0** | 비활성 회원 로그인 가능 | 보안 | **수정 완료** |
| **P1** | ShouldNotFilterPath 글로브 패턴 | 버그 | **수정 완료** |
| **P1** | `join()` 예외 메시지 포맷 오류 | 버그 | **수정 완료** |
| **P1** | Cookie Secure/SameSite 미설정 | 보안 | **수정 완료** |
| **P1** | 비밀번호 패턴 미검증 | 보안 | **수정 완료** |
| **P1** | 로그인 브루트포스 방어 | 보안 | **수정 완료** |
| **P2** | Refresh Token Rotation | 보안 | **수정 완료** |
| **P2** | 로그아웃 토큰 무효화 | 보안 | 미수정 |
| **P2** | AuthService SRP 분리 | 설계 | 미수정 |
| **P2** | JwtUtil 구조 개선 | 설계 | 미수정 |
| **P2** | jjwt Deprecated API | 설계 | 미수정 |
| **P3** | CurrentMemberIdResolver null 반환 | 설계 | 미수정 |
| **P3** | deleteMember 에러 처리 | 설계 | 미수정 |
