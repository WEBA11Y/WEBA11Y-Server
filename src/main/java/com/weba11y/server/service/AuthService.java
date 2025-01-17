package com.weba11y.server.service;

import com.weba11y.server.dto.member.JoinDto;
import com.weba11y.server.dto.member.JoinResultDto;
import com.weba11y.server.dto.member.LoginDto;
import com.weba11y.server.dto.member.LoginResultDto;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    LoginResultDto login(LoginDto loginDto, HttpServletResponse response);

    JoinResultDto join(JoinDto joinDto);

    boolean isExistsUsername(String username);

    boolean isExistsEmail(String username);

    boolean isExistsPhoneNum(String username);
}
