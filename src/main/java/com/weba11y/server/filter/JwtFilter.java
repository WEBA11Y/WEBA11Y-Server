package com.weba11y.server.filter;

import com.weba11y.server.constants.ShouldNotFilterPath;
import com.weba11y.server.dto.member.TokenInfo;
import com.weba11y.server.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final String secret;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        logRequestDetails(request);

        try {
            extractToken(request)
                    .flatMap(this::validateAndParseToken)
                    .ifPresent(tokenInfo -> setAuthentication(tokenInfo, request));
        } catch (ExpiredJwtException e) {
            log.warn("Token has expired: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("Invalid token: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private void logRequestDetails(HttpServletRequest request) {
        String clientIp = getClientIp(request);
        String method = request.getMethod();
        String uri = request.getRequestURI();
        log.info("Request: [{}] {} - IP: {}", method, uri, clientIp);
    }

    private Optional<String> extractToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION))
                .filter(header -> header.startsWith("Bearer "))
                .map(header -> header.substring(7));
    }

    private Optional<TokenInfo> validateAndParseToken(String token) {
        try {
            JwtUtil.validateToken(token, secret);
            return Optional.of(JwtUtil.getTokenInfo(token, secret));
        } catch (Exception e) {
            throw e;
        }
    }

    private void setAuthentication(TokenInfo tokenInfo, HttpServletRequest request) {
        log.info("Member_ID: {}, Role: {}, IP: {}", tokenInfo.getMemberId(), tokenInfo.getRole(), getClientIp(request));

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                tokenInfo.getMemberId(), null, List.of(new SimpleGrantedAuthority(tokenInfo.getRole())));
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isEmpty()) {
            return xfHeader.split(",")[0];
        }
        return request.getRemoteAddr();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return ShouldNotFilterPath.EXCLUDE_PATHS.stream().anyMatch(path::startsWith);
    }
}