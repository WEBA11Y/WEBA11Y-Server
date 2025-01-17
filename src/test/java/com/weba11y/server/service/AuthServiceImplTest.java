package com.weba11y.server.service;


import com.weba11y.server.domain.Member;
import com.weba11y.server.dto.member.JoinDto;
import com.weba11y.server.dto.member.LoginDto;
import com.weba11y.server.repository.MemberRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
public class AuthServiceImplTest {

    @Autowired
    private MemberRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void before() {
        Member newMember = Member.builder()
                .username("moyada")
                .password(passwordEncoder.encode("moyada"))
                .name("moya")
                .email("moya@weba11y.com")
                .birthday(LocalDate.now())
                .phoneNum("010-1111-1111")
                .build();
        repository.save(newMember);
    }

    @Test
    @DisplayName("로그인 테스트")
    void login() {
        // given
        // 아이디 비밀번호 모두 올바름
        LoginDto loginDto = LoginDto.builder()
                .username("moyada")
                .password("moyada")
                .build();
        // 아이디는 맞지만 비밀번호 틀림
        LoginDto fakeDto1 = LoginDto.builder()
                .username("moyada")
                .password("moyada123")
                .build();
        // 존재하지 않는 아이디
        LoginDto fakeDto2 = LoginDto.builder()
                .username("moyada33")
                .password("moyada")
                .build();
        // when
        Member findMember1 = getMemberByUsername(loginDto.getUsername());
        Member findMember2 = getMemberByUsername(fakeDto1.getUsername());

        // then
        // result 1
        assertTrue(isMatchPassword(loginDto, findMember1));
        // result 2
        assertFalse(isMatchPassword(fakeDto1, findMember2));
        // result 3
        assertThrows(NoSuchElementException.class, () -> getMemberByUsername(fakeDto2.getUsername()));
    }

    private Member getMemberByUsername(String username) {
        return repository.findByUsername(username)
                .orElseThrow(()
                        -> new NoSuchElementException("존재하지 않는 회원입니다"));
    }

    private boolean isMatchPassword(LoginDto loginDto, Member findMember) {
        return passwordEncoder.matches(loginDto.getPassword(), findMember.getPassword());
    }

    @Test
    @DisplayName("회원 등록 테스트")
    void join() {

    }
}
