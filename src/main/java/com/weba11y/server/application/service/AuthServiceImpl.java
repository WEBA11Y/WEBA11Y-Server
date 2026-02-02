package com.weba11y.server.application.service;

import com.weba11y.server.domain.member.Member;
import com.weba11y.server.domain.member.Token;
import com.weba11y.server.api.dto.member.*;
import com.weba11y.server.global.exception.custom.DuplicateFieldException;
import com.weba11y.server.global.exception.custom.ExpiredRefreshTokenException;
import com.weba11y.server.global.exception.custom.ExpiredTokenException;
import com.weba11y.server.infrastructure.persistence.MemberRepository;
import com.weba11y.server.application.service.AuthService;
import com.weba11y.server.infrastructure.security.CookieUtil;
import com.weba11y.server.infrastructure.security.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

import static com.weba11y.server.infrastructure.security.CookieName.REFRESH_TOKEN_COOKIE;

@Slf4j
@Service
@Transactional(value = "transactionManager", readOnly = true)
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final MemberRepository repository;

    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiration}")
    private Long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private Long refreshTokenExpiration;

    @Override
    @Transactional(value = "transactionManager")
    public JoinResultDto join(JoinDto joinDto) {
        // Unique 값 검사
        validateUniqueMemberInfo(joinDto);

        Member newMember = joinDto.toEntity(passwordEncoder);

        try {
            Member saveMember = repository.save(newMember);
            return JoinResultDto.builder()
                    .message("회원가입이 완료되었습니다.")
                    .id(saveMember.getId())
                    .userId(saveMember.getUserId())
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("회원가입 중 오류발생 {}");
        }
    }

    @Override
    public LoginResultDto login(LoginDto loginDto, HttpServletResponse response) {
        Member findMember = getMemberByUserId(loginDto.getUserId());
        if (!isPasswordMatching(loginDto, findMember))
            throw new BadCredentialsException("비밀번호가 일치하지 않습니다");

        Token token = createAuthenticationToken(findMember);
        Cookie refreshTokenCookie = CookieUtil.addCookie(REFRESH_TOKEN_COOKIE, token.getRefresh_token());
        response.addCookie(refreshTokenCookie);

        return LoginResultDto.builder()
                .message("로그인을 성공했습니다.")
                .id(findMember.getId())
                .userId(findMember.getUserId())
                .accessToken(token.getAccess_token())
                .build();
    }

    @Override
    public Member retrieveMember(Long memberId) {
        return repository.findById(memberId).orElseThrow(()
                -> new NoSuchElementException("회원 정보가 존재하지 않습니다."));
    }

    @Override
    @Transactional(value = "transactionManager")
    public MemberDto updateMember(Long memberId, UpdateMemberDto updateMemberDto) {
        Member member = retrieveMember(memberId);
        if (isExistsPhoneNum(updateMemberDto.getPhoneNum())) {
            throw new DuplicateFieldException("이미 사용 중인 전화번호입니다.");
        }
        member.update(updateMemberDto.getPhoneNum());
        return MemberDto.of(member);
    }

    @Override
    @Transactional(value = "transactionManager")
    public String deleteMember(Long memberId) {
        Member member = retrieveMember(memberId);
        try {
            member.delete();
            return "회원 탈퇴 성공";
        } catch (Exception e) {
            return "회원 탈퇴 실패";
        }
    }


    private Member getMemberByUserId(String userId) {
        return repository.findByUserId(userId)
                .orElseThrow(()
                        -> new NoSuchElementException("존재하지 않는 회원입니다"));
    }

    private void validateUniqueMemberInfo(JoinDto joinDto) {
        if (!repository.existsUniqueInfo(joinDto)) {
            throw new DuplicateFieldException("중복된 회원정보입니다.");
        }
    }

    @Override
    public boolean isExistsUserId(String userId) {
        return repository.existsByUserId(userId);
    }

    @Override
    public boolean isExistsPhoneNum(String phoneNum) {
        return repository.existsByPhoneNum(phoneNum);
    }

    @Override
    public String reissuingAccessToken(String refreshToken) {
        // Refresh Token 검증.
        try {
            JwtUtil.validateToken(refreshToken, secret);
        } catch (ExpiredTokenException e) {
            // RefreshToken 만료시 Data 삭제.
            throw new ExpiredRefreshTokenException("토큰이 만료되었습니다.");
        }
        // Access Token 재발급
        return JwtUtil.reissuingToken(getTokenInfo(refreshToken), accessTokenExpiration, secret);
    }

    @Override
    public TokenInfo getTokenInfo(String token) {
        tokenIsExpired(token);
        return JwtUtil.getTokenInfo(token, secret);
    }

    @Override
    public boolean tokenIsExpired(String token) {
        try {
            JwtUtil.validateToken(token, secret); // 토큰 검증
            return true;
        } catch (ExpiredTokenException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    // 비밀번호
    private boolean isPasswordMatching(LoginDto loginDto, Member findMember) {
        return passwordEncoder.matches(loginDto.getPassword(), findMember.getPassword());
    }

    // JWT 생성
    private Token createAuthenticationToken(Member member) {
        return JwtUtil.createToken(member, accessTokenExpiration, refreshTokenExpiration, secret);
    }
}
