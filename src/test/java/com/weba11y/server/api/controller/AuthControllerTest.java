package com.weba11y.server.api.controller;

import com.weba11y.server.domain.member.Member;
import com.weba11y.server.api.dto.member.JoinDto;
import com.weba11y.server.api.dto.member.LoginDto;
import com.weba11y.server.api.dto.member.UpdateMemberDto;
import com.weba11y.server.infrastructure.persistence.MemberRepository;
import com.weba11y.server.infrastructure.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AuthControllerTest extends BaseIntegrationTest {
    @Autowired
    private MemberRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String TEST_ID = "test1111";
    private static final String TEST_PW = "test1111";

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiration}")
    private Long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private Long refreshTokenExpiration;

    private static Member member;

    private static String accessToken;

    @BeforeEach
    void before() {
        member = repository.save(Member.builder()
                .userId(TEST_ID)
                .password(passwordEncoder.encode(TEST_PW))
                .name("Test111")
                .birthday(LocalDate.parse("1996-08-19"))
                .phoneNum("010-1112-2221")
                .build());

        accessToken = JwtUtil.createToken(member, accessTokenExpiration, refreshTokenExpiration, secret)
                .getAccess_token();
    }

    @Test
    @DisplayName("회원 가입")
    void memberJoin() throws Exception {
        // given
        JoinDto joinDto = JoinDto.builder()
                .userId("test1234")
                .password("test1234")
                .name("Test")
                .phoneNum("01011112222")
                .birthday(LocalDate.parse("1996-08-19"))
                .build();

        // when
        ResultActions resultActions = mockMvc.perform(post("/api/v1/join")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(joinDto)));

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("회원가입이 완료되었습니다."))
                .andExpect(jsonPath("$.userId").value(joinDto.getUserId()));

    }

    @Test
    @DisplayName("로그인")
    void memberLogin() throws Exception {
        // given
        LoginDto loginDto = LoginDto.builder()
                .userId(TEST_ID)
                .password(TEST_PW)
                .build();
        // when
        ResultActions resultActions = mockMvc.perform(post("/api/v1/login")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(loginDto)));

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(loginDto.getUserId()));

    }

    @Test
    @DisplayName("회원 정보 조회")
    void getMember() throws Exception {
        // given

        // when
        ResultActions resultActions = mockMvc.perform(get("/api/v1/member")
                .header("Authorization", "Bearer " + accessToken));
        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(member.getUserId()));
    }

    @Test
    @DisplayName("회원 정보 수정")
    void updateMember() throws Exception {
        // given
        UpdateMemberDto memberDto = UpdateMemberDto.builder()
                .phoneNum("010-3333-2222")
                .build();

        // when
        ResultActions resultActions = mockMvc.perform(put("/api/v1/member")
                .header("Authorization", "Bearer " + accessToken)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(memberDto)));
        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.phoneNum").value(memberDto.getPhoneNum()));
    }

    @Test
    @DisplayName("회원 탈퇴")
    void deleteMember() throws Exception {
        // given

        // when
        ResultActions resultActions = mockMvc.perform(delete("/api/v1/member")
                .header("Authorization", "Bearer " + accessToken)
        );
        // then
        resultActions.andExpect(status().isOk());
    }

    @Test
    @DisplayName("아이디 중복 조회")
    void checkUserIdExists() throws Exception {
        // given
        String userId = TEST_ID; // 중복된 아이디
        String userId2 = "test1343"; // 중복되지 않은 아이디
        // when
        ResultActions resultActions = mockMvc.perform(get("/api/v1/join/check-userId")
                .param("userId", userId));

        ResultActions resultActions2 = mockMvc.perform(get("/api/v1/join/check-userId")
                .param("userId", userId2));
        // then
        resultActions.andExpect(status().isConflict());
        resultActions2.andExpect(status().isOk());
    }

    @Test
    @DisplayName("전화번호 중복 조회")
    void checkPhoneNumExists() throws Exception {
        // given
        String phone = member.getPhoneNum(); // 중복된 전화번호
        String phone2 = "010-1234-4321"; // 중복되지 않은 전화번호
        // when
        ResultActions resultActions = mockMvc.perform(get("/api/v1/join/check-phone")
                .param("phone", phone));

        ResultActions resultActions2 = mockMvc.perform(get("/api/v1/join/check-phone")
                .param("phone", phone2));
        // then
        resultActions.andExpect(status().isConflict());
        resultActions2.andExpect(status().isOk());
    }


}
