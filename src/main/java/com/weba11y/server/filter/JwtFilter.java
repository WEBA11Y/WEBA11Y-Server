package com.weba11y.server.filter;

import com.weba11y.server.constants.ShouldNotFilterPath;
import com.weba11y.server.dto.member.TokenInfo;
import com.weba11y.server.exception.custom.ExpiredTokenException;
import com.weba11y.server.service.AuthService;
import com.weba11y.server.util.JwtUtil;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
public class JwtFilter extends OncePerRequestFilter {
    private final String secret;
    private final AuthService authService;

    public JwtFilter(AuthService authService, String secret) {
        this.authService = authService;
        this.secret = secret;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String clientIp = getClientIp(request);
        final String method = request.getMethod();
        final String uri = request.getRequestURI();

        log.info("Request: [{}] {} - IP: {}", method, uri, clientIp);

        // Token이 없을 시 Block
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Token 꺼내기
        String token = authorization.split(" ")[1];
        if (token.isEmpty()) {
            log.error("Token is null - IP: {}", clientIp);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "토큰이 존재하지 않습니다.");
            return;
        }

        // Token Expired 여부 검사
        try {
            JwtUtil.validateToken(token, secret);
        } catch (SignatureException | ExpiredTokenException | MalformedJwtException e) {
            log.warn("Invalid token - IP: {}, Reason: {}", clientIp, e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
            return;
        }

        // Token에 저장된 정보
        TokenInfo tokenInfo = JwtUtil.getTokenInfo(token, secret);
        Long memberPrimaryKey = tokenInfo.getMemberId();

        log.info("Member_ID: {}, Role: {}, IP: {}", memberPrimaryKey, tokenInfo.getRole(), clientIp);

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(memberPrimaryKey, null, List.of(new SimpleGrantedAuthority(tokenInfo.getRole())));

        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        filterChain.doFilter(request, response);
    }
    // 쿠키에서 토큰을 추출하는 헬퍼 메소드
    private Optional<String> extractTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            return Arrays.stream(cookies)
                    .filter(cookie -> "Authorization".equals(cookie.getName()) || "AccessToken".equals(cookie.getName())) // "Authorization" 또는 "AccessToken" 쿠키 이름 사용
                    .map(Cookie::getValue)
                    .filter(value -> value.startsWith("Bearer ")) // "Bearer " 접두사 확인
                    .map(value -> value.substring(7)) // "Bearer " 제거
                    .findFirst();
        }
        return Optional.empty();
    }
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return (ShouldNotFilterPath.EXCLUDE_PATHS.stream().anyMatch(path::startsWith));
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isEmpty()) {
            return xfHeader.split(",")[0];  // 첫 번째가 실제 클라이언트 IP
        }
        return request.getRemoteAddr();
    }
}
