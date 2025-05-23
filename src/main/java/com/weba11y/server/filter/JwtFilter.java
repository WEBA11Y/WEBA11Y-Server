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
import java.util.List;

@Slf4j
public class JwtFilter extends OncePerRequestFilter {
    private String secret;
    private AuthService authService;

    public JwtFilter(AuthService authService, String secret) {
        this.authService = authService;
        this.secret = secret;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        final String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        log.info("HTTP Method : {}", request.getMethod());

        // Token이 없을 시 Block
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        // Token 꺼내기
        String token = authorization.split(" ")[1];
        if (token.isEmpty()) {
            log.error("Token is null");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "토큰이 존재하지 않습니다.");
        }

        // Token Expired되었는지 여부
        try {
            JwtUtil.validateToken(token, secret);
        } catch (SignatureException e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
            return;
        } catch (ExpiredTokenException e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
            return;
        } catch (MalformedJwtException e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
            return;
        }

        // Token에 저장된 정보
        TokenInfo tokenInfo = JwtUtil.getTokenInfo(token, secret);
        Long memberPrimaryKey = tokenInfo.getMemberId();
        // 권한 지정
        log.info("Member_Primary_Key : {}", memberPrimaryKey);
        log.info("Role : {}", tokenInfo.getRole());

        // 권한 부여
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(memberPrimaryKey, null, List.of(new SimpleGrantedAuthority(tokenInfo.getRole())));

        // Detail을 넣어준다.
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        log.info("Path : {}", path);
        return (ShouldNotFilterPath.EXCLUDE_PATHS.stream().anyMatch(path::startsWith));
    }
}
