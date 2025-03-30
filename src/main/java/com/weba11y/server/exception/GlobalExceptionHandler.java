package com.weba11y.server.exception;

import com.weba11y.server.exception.custom.*;
import com.weba11y.server.util.CookieUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.persistence.PersistenceException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;
import java.security.SignatureException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 잘못된 URL 입력
    @ExceptionHandler(InvalidUrlException.class)
    public ResponseEntity<?> handleInvalidUrlException(InvalidUrlException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

    // 중복된 URL 입력
    @ExceptionHandler(DuplicationUrlException.class)
    public ResponseEntity<?> handleDuplicationUrlException(DuplicationUrlException e) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", e.getMessage());
        response.put("urlId", e.getUrlId());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(DuplicateFieldException.class)
    public ResponseEntity<?> handleDuplicateFieldException(DuplicateFieldException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }

    // 잘못된 비밀 번호
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handleBadCredentialsException(BadCredentialsException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

    // Token 예외 처리
    @ExceptionHandler({AccessDeniedException.class, SignatureException.class, ExpiredTokenException.class, ExpiredJwtException.class})
    public ResponseEntity<?> handleUnauthorizedAccessException(Exception e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
    }

    // RefreshToken 만료
    @ExceptionHandler(ExpiredRefreshTokenException.class)
    public ResponseEntity<?> handleExpiredRefreshTokenException(HttpServletRequest request, HttpServletResponse response, ExpiredRefreshTokenException e) {
        Cookie refreshTokenCookie = CookieUtil.findCookie(request, "refresh_token_key");
        CookieUtil.deleteCookie(response, refreshTokenCookie);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
    }

    // Internal Sever Error
    @ExceptionHandler({RuntimeException.class, PersistenceException.class})
    public ResponseEntity<?> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
}
