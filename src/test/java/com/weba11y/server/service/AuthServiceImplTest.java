package com.weba11y.server.service;


import com.weba11y.server.domain.Member;
import com.weba11y.server.dto.member.LoginDto;
import com.weba11y.server.jpa.repository.MemberRepository;
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
                .userId("moyada")
                .password(passwordEncoder.encode("moyada"))
                .name("moya")
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
                .userId("moyada")
                .password("moyada")
                .build();
        // 아이디는 맞지만 비밀번호 틀림
        LoginDto fakeDto1 = LoginDto.builder()
                .userId("moyada")
                .password("moyada123")
                .build();
        // 존재하지 않는 아이디
        LoginDto fakeDto2 = LoginDto.builder()
                .userId("moyada33")
                .password("moyada")
                .build();
        // when
        Member findMember1 = getMemberByUserId(loginDto.getUserId());
        Member findMember2 = getMemberByUserId(fakeDto1.getUserId());

        // then
        // result 1
        assertTrue(isMatchPassword(loginDto, findMember1));
        // result 2
        assertFalse(isMatchPassword(fakeDto1, findMember2));
        // result 3
        assertThrows(NoSuchElementException.class, () -> getMemberByUserId(fakeDto2.getUserId()));
    }

    private Member getMemberByUserId(String userId) {
        return repository.findByUserId(userId)
                .orElseThrow(()
                        -> new NoSuchElementException("존재하지 않는 회원입니다"));
    }

    private boolean isMatchPassword(LoginDto loginDto, Member findMember) {
        return passwordEncoder.matches(loginDto.getPassword(), findMember.getPassword());
    }

}
