package com.weba11y.server.application.service;

import com.weba11y.server.domain.member.Member;
import com.weba11y.server.api.dto.member.*;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    LoginResultDto login(LoginDto loginDto, HttpServletResponse response);

    JoinResultDto join(JoinDto joinDto);

    boolean isExistsUserId(String userId);

    boolean isExistsPhoneNum(String username);

    Member retrieveMember(Long memberId);

    MemberDto updateMember(Long memberId, UpdateMemberDto updateMemberDto);

    String deleteMember(Long memberId);

    String reissuingAccessToken(String refreshToken);

    TokenInfo getTokenInfo(String token);

    boolean tokenIsExpired(String token);
}
