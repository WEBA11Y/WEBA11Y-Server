package com.weba11y.server.api.controller;


import com.weba11y.server.domain.member.Member;
import com.weba11y.server.api.dto.inspectionUrl.InspectionUrlDto;
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

public class InspectionUrlControllerTest extends BaseIntegrationTest {
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
    @DisplayName("URL 등록")
    void registerUrl() throws Exception {
        // given
        InspectionUrlDto.Request request = InspectionUrlDto.Request.builder()
                .url("https://www.naver.com")
                .description("Naver")
                .build();
        // when
        ResultActions resultActions = mockMvc.perform(post("/api/v1/urls")
                .header("Authorization", "Bearer " + accessToken)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)));

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value(request.getUrl()))
                .andExpect(jsonPath("$.description").value(request.getDescription()));
    }

    @Test
    @DisplayName("모든 URL 조회")
    void getAllUrl() throws Exception {
        // given

        // when
        ResultActions resultActions = mockMvc.perform(get("/api/v1/urls")
                .header("Authorization", "Bearer " + accessToken));
        // then
        resultActions.andExpect(status().isOk());
    }
}
