package com.weba11y.server.controller;


import com.weba11y.server.dto.member.JoinDto;
import com.weba11y.server.dto.member.LoginDto;
import com.weba11y.server.dto.member.MemberDto;
import com.weba11y.server.dto.member.UpdateMemberDto;
import com.weba11y.server.exception.custom.InvalidateTokenException;
import com.weba11y.server.service.AuthService;
import com.weba11y.server.util.CookieUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

import static com.weba11y.server.util.CookieUtil.*;


@RestController
@RequiredArgsConstructor
@Tag(name = "회원 및 인증 API", description = "회원 관리 및 인증 관련 API")
public class AuthController {

    private final AuthService authService;

    private static final String REFRESH_TOKEN_COOKIE = "refresh_token";

    @PostMapping("/api/v1/login")
    @Operation(summary = "로그인", description = "로그인 성공시 인증 토큰을 발급합니다.")
    public ResponseEntity<?> memberLogin(@RequestBody @Valid LoginDto loginDto,
                                         HttpServletResponse res) {
        return ResponseEntity.ok().body(authService.login(loginDto, res));
    }

    @PostMapping("/api/v1/join")
    @Operation(summary = "회원가입", description = "입력한 회원 정보를 등록합니다.")
    public ResponseEntity<?> memberJoin(@RequestBody @Valid JoinDto joinDto) {
        return ResponseEntity.ok().body(authService.join(joinDto));
    }

    @GetMapping("/api/v1/member")
    @Operation(summary = "회원 정보 조회", description = "인증 토큰으로 회원 정보를 가져옵니다.")
    public ResponseEntity<MemberDto> getMember(Principal principal) {
        Long memberId = getMemberId(principal);
        return ResponseEntity.ok().body(authService.retrieveMember(memberId).toDto());
    }

    @PutMapping("/api/v1/member")
    @Operation(summary = "회원 정보 수정", description = "수정 가능한 회원 정보를 수정합니다.")
    public ResponseEntity<?> updateMember(@RequestBody @Valid UpdateMemberDto memberDto,
                                          Principal principal) {
        Long memberId = getMemberId(principal);
        return ResponseEntity.ok().body(authService.updateMember(memberId, memberDto));
    }

    @DeleteMapping("/api/v1/member")
    @Operation(summary = "회원 탈퇴", description = "회원을 비활성화 하고 30일 이후에 영구적으로 삭제합니다.")
    public ResponseEntity<?> deleteMember(Principal principal) {
        Long memberId = getMemberId(principal);
        return ResponseEntity.ok().body(authService.deleteMember(memberId));
    }


    @GetMapping("/api/v1/join/check-userId")
    @Operation(summary = "아이디 중복 조회", description = "중복된 아이디가 있는지 확인합니다.")
    public ResponseEntity<Void> checkUserIdExists(@RequestParam("userId") @Valid String userId) {
        return authService.isExistsUserId(userId)
                ? ResponseEntity.status(HttpStatus.CONFLICT).build()
                : ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/api/v1/join/check-phone")
    @Operation(summary = "전화번호 중복 조회", description = "중복된 전화번호가 있는지 확인합니다.")
    public ResponseEntity<Void> checkPhoneNumExists(@RequestParam("phone") @Valid String phoneNum) {
        return authService.isExistsPhoneNum(phoneNum)
                ? ResponseEntity.status(HttpStatus.CONFLICT).build()
                : ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/api/v1/reissuing-token")
    public ResponseEntity<String> reissuingAccessToken(HttpServletRequest request) {
        Cookie refreshTokenCookie = findCookie(request, REFRESH_TOKEN_COOKIE);
        if (refreshTokenCookie == null || refreshTokenCookie.getValue().equals(""))
            throw new InvalidateTokenException("토큰이 존재하지 않습니다.");
        return ResponseEntity.ok().body(authService.reissuingAccessToken(refreshTokenCookie.getValue()));
    }

    // 로그아웃
    @GetMapping("/api/v1/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        // Cookie 에서 Refresh Token 찾는다.
        Cookie refreshTokenCookie = CookieUtil.findCookie(request, REFRESH_TOKEN_COOKIE);
        if (refreshTokenCookie != null && refreshTokenCookie.getValue() != null && !refreshTokenCookie.getValue().equals(""))
            CookieUtil.deleteCookie(response, refreshTokenCookie); // Refresh Token 삭제 -> Refresh Token의 정보 소멸
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private Long getMemberId(Principal principal) {
        return principal instanceof UsernamePasswordAuthenticationToken
                ? (Long) ((UsernamePasswordAuthenticationToken) principal).getPrincipal()
                : -1L;
    }
}
