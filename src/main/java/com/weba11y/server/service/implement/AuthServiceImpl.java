package com.weba11y.server.service.implement;

import com.weba11y.server.domain.Member;
import com.weba11y.server.domain.Token;
import com.weba11y.server.dto.member.*;
import com.weba11y.server.exception.custom.DuplicateFieldException;
import com.weba11y.server.repository.MemberRepository;
import com.weba11y.server.service.AuthService;
import com.weba11y.server.util.CookieUtil;
import com.weba11y.server.util.JwtUtil;
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

import static com.weba11y.server.constants.CookieName.REFRESH_TOKEN_COOKIE;

@Slf4j
@Service
@Transactional(readOnly = true)
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
    @Transactional
    public JoinResultDto join(JoinDto joinDto) {
        // Unique 값 검사
        validateUniqueMemberInfo(joinDto);
        Member newMember = Member.builder()
                .username(joinDto.getUsername())
                .password(encodingPassword(joinDto.getPassword())) // 암호화 된 비밀번호
                .name(joinDto.getName())
                .email(joinDto.getEmail())
                .phoneNum(joinDto.getPhoneNum())
                .birthday(joinDto.getBirthday())
                .build();
        try {
            Member saveMember = repository.save(newMember);
            return JoinResultDto.builder()
                    .message("회원가입이 완료되었습니다.")
                    .id(saveMember.getId())
                    .username(saveMember.getUsername())
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("회원가입 중 오류발생 {}");
        }
    }

    @Override
    public LoginResultDto login(LoginDto loginDto, HttpServletResponse response) {
        Member findMember = getMemberByUsername(loginDto.getUsername());
        if (!isPasswordMatching(loginDto, findMember))
            throw new BadCredentialsException("비밀번호가 일치하지 않습니다");

        Token token = createAuthenticationToken(findMember);
        Cookie refreshTokenCookie = CookieUtil.addCookie(REFRESH_TOKEN_COOKIE, token.getRefresh_token());
        response.addCookie(refreshTokenCookie);

        return LoginResultDto.builder()
                .message("로그인을 성공했습니다.")
                .id(findMember.getId())
                .username(findMember.getUsername())
                .accessToken(token.getAccess_token())
                .build();
    }

    @Override
    public MemberDto retrieveMember(Long memberId) {
        return MemberDto.of(getMemberById(memberId));
    }

    @Override
    @Transactional
    public MemberDto updateMember(Long memberId, UpdateMemberDto updateMemberDto) {
        Member member = getMemberById(memberId);
        if (isExistsEmail(updateMemberDto.getEmail())) {
            throw new DuplicateFieldException("이미 사용 중인 이메일입니다.");
        }
        if (isExistsPhoneNum(updateMemberDto.getPhoneNum())) {
            throw new DuplicateFieldException("이미 사용 중인 전화번호입니다.");
        }
        member.update(updateMemberDto.getEmail(), updateMemberDto.getPhoneNum());
        return MemberDto.of(member);
    }

    @Override
    @Transactional
    public String deleteMember(Long memberId) {
        Member member = getMemberById(memberId);
        try {
            member.delete();
            return "회원 탈퇴 성공";
        } catch (Exception e) {
            return "회원 탈퇴 실패";
        }
    }

    private Member getMemberById(Long memberId) {
        return repository.findById(memberId).orElseThrow(()
                -> new NoSuchElementException("회원 정보가 존재하지 않습니다."));
    }

    private Member getMemberByUsername(String username) {
        return repository.findByUsername(username)
                .orElseThrow(()
                        -> new NoSuchElementException("존재하지 않는 회원입니다"));
    }

    private void validateUniqueMemberInfo(JoinDto joinDto) {
        if (!repository.existsUniqueInfo(joinDto)) {
            throw new DuplicateFieldException("중복된 회원정보입니다.");
        }
    }

    @Override
    public boolean isExistsUsername(String username) {
        return repository.existsByUsername(username);
    }

    @Override
    public boolean isExistsEmail(String email) {
        return repository.existsByEmail(email);
    }

    @Override
    public boolean isExistsPhoneNum(String phoneNum) {
        return repository.existsByPhoneNum(phoneNum);
    }

    private String encodingPassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
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
