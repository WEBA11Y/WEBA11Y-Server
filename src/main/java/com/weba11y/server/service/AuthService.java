package com.weba11y.server.service;

import com.weba11y.server.domain.Member;
import com.weba11y.server.dto.member.*;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    LoginResultDto login(LoginDto loginDto, HttpServletResponse response);

    JoinResultDto join(JoinDto joinDto);

    boolean isExistsUsername(String username);

    boolean isExistsEmail(String username);

    boolean isExistsPhoneNum(String username);

    Member retrieveMember(Long memberId);

    MemberDto updateMember(Long memberId, UpdateMemberDto updateMemberDto);

    String deleteMember(Long memberId);
}
