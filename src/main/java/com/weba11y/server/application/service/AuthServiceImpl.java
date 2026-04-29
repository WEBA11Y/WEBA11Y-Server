package com.weba11y.server.application.service;

import com.weba11y.server.domain.member.Member;
import com.weba11y.server.domain.member.Token;
import com.weba11y.server.api.dto.member.*;
import com.weba11y.server.domain.enums.MemberStatus;
import com.weba11y.server.global.exception.custom.AccountLockedException;
import com.weba11y.server.global.exception.custom.DeactivatedMemberException;
import com.weba11y.server.global.exception.custom.DuplicateFieldException;
import com.weba11y.server.global.exception.custom.ExpiredRefreshTokenException;
import com.weba11y.server.global.exception.custom.ExpiredTokenException;
import com.weba11y.server.infrastructure.persistence.MemberRepository;
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
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final MemberRepository repository;

    private final PasswordEncoder passwordEncoder;

    private final LoginAttemptService loginAttemptService;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiration}")
    private Long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private Long refreshTokenExpiration;

    @Override
    @Transactional
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
            throw new RuntimeException("회원가입 중 오류발생: " + e.getMessage(), e);
        }
    }

    @Override
    public LoginResultDto login(LoginDto loginDto, HttpServletResponse response) {
        String userId = loginDto.getUserId();

        // 브루트포스 방어: 로그인 시도 횟수 초과 시 차단
        if (loginAttemptService.isBlocked(userId)) {
            throw new AccountLockedException("로그인 시도 횟수를 초과했습니다. 30분 후에 다시 시도해주세요.");
        }

        Member findMember = getMemberByUserId(userId);

        if (!isPasswordMatching(loginDto, findMember)) {
            loginAttemptService.loginFailed(userId);
            throw new BadCredentialsException("아이디 또는 비밀번호가 일치하지 않습니다.");
        }

        if (findMember.getStatus() == MemberStatus.DEACTIVATED) {
            throw new DeactivatedMemberException("비활성화된 계정입니다. 관리자에게 문의하세요.");
        }

        // 로그인 성공 시 실패 횟수 초기화
        loginAttemptService.loginSucceeded(userId);

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
    @Transactional
    public MemberDto updateMember(Long memberId, UpdateMemberDto updateMemberDto) {
        Member member = retrieveMember(memberId);
        if (repository.existsByPhoneNumAndIdNot(updateMemberDto.getPhoneNum(), memberId)) {
            throw new DuplicateFieldException("이미 사용 중인 전화번호입니다.");
        }
        member.update(updateMemberDto.getPhoneNum());
        return MemberDto.of(member);
    }

    @Override
    @Transactional
    public void deleteMember(Long memberId) {
        Member member = retrieveMember(memberId);
        member.delete();
    }

    @Override
    @Transactional
    public MemberDto deactivateMember(Long memberId) {
        Member member = retrieveMember(memberId);
        member.deactivate();
        return MemberDto.of(member);
    }

    @Override
    @Transactional
    public MemberDto activateMember(Long memberId) {
        Member member = retrieveMember(memberId);
        member.activate();
        return MemberDto.of(member);
    }


    private Member getMemberByUserId(String userId) {
        return repository.findByUserId(userId)
                .orElseThrow(()
                        -> new BadCredentialsException("아이디 또는 비밀번호가 일치하지 않습니다."));
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
    public String reissuingAccessToken(String refreshToken, HttpServletResponse response) {
        // Refresh Token 검증.
        try {
            JwtUtil.validateToken(refreshToken, secret);
        } catch (ExpiredTokenException e) {
            // RefreshToken 만료시 Data 삭제.
            throw new ExpiredRefreshTokenException("토큰이 만료되었습니다.");
        }

        TokenInfo tokenInfo = getTokenInfo(refreshToken);

        // Refresh Token Rotation: 새로운 Refresh Token 발급
        String newRefreshToken = JwtUtil.reissuingToken(tokenInfo, refreshTokenExpiration, secret);
        Cookie newRefreshTokenCookie = CookieUtil.addCookie(REFRESH_TOKEN_COOKIE, newRefreshToken);
        response.addCookie(newRefreshTokenCookie);

        // Access Token 재발급
        return JwtUtil.reissuingToken(tokenInfo, accessTokenExpiration, secret);
    }

    @Override
    public TokenInfo getTokenInfo(String token) {
        if (!isTokenValid(token)) {
            throw new ExpiredTokenException("토큰이 만료되었습니다.");
        }
        return JwtUtil.getTokenInfo(token, secret);
    }

    @Override
    public boolean isTokenValid(String token) {
        try {
            JwtUtil.validateToken(token, secret);
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
