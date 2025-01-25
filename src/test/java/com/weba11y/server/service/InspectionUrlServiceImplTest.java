package com.weba11y.server.service;


import com.weba11y.server.domain.InspectionUrl;
import com.weba11y.server.domain.Member;
import com.weba11y.server.dto.InspectionUrl.InspectionUrlRequestDto;
import com.weba11y.server.repository.InspectionUrlRepository;
import com.weba11y.server.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.LocalDate;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
public class InspectionUrlServiceImplTest {
    @Autowired
    InspectionUrlRepository inspectionUrlRepository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    private Validator validator;

    private static Member member;
    private static InspectionUrl parentUrl;

    @BeforeEach
    void before() {
        // Member 생성
        Member newMember = Member.builder()
                .username("test")
                .password("test1234")
                .name("test")
                .birthday(LocalDate.now())
                .email("test@test.com")
                .phoneNum("01011112222")
                .build();

        member = memberRepository.save(newMember);


        // parent URL 생성
        InspectionUrl parent = InspectionUrl.builder()
                .title("Test")
                .url("https://www.test.com")
                .member(newMember)
                .build();

        parentUrl = inspectionUrlRepository.save(parent);
    }

    @Test
    @DisplayName("URL 등록")
    void URL_등록() {
        // given
        InspectionUrlRequestDto newUrlDto = InspectionUrlRequestDto.builder()
                .title("Naver")
                .url("https://www.naver.com")
                .build();
        // when
        InspectionUrl newUrl = newUrlDto.toEntity(member);

        InspectionUrl savedUrl = inspectionUrlRepository.save(newUrl);

        // then
        assertThat(savedUrl.getId()).isEqualTo(newUrl.getId());
    }

    @Test
    @DisplayName("잘못된 형식의 URL 등록")
    void 잘못된_URL_등록() {
        // given
        InspectionUrlRequestDto dto = InspectionUrlRequestDto.builder()
                .title("Naver")
                .url("www.naver.com") // 잘못된 URL
                .build();

        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(dto, "dto");

        // when
        validator.validate(dto, bindingResult);

        // then
        assertThrows(MethodArgumentNotValidException.class, () -> {
            if (bindingResult.hasErrors()) {
                throw new MethodArgumentNotValidException(null, bindingResult); // 예외 발생
            }
        });
    }

    @Test
    @DisplayName("자식 URL 등록")
    void Child_URL_등록() {
        // given
        InspectionUrlRequestDto childUrl = InspectionUrlRequestDto.builder()
                .title("Naver")
                .url("https://www.naver.com")
                .parentId(parentUrl.getId())
                .build();
        // when
        InspectionUrl parent = inspectionUrlRepository.findById(childUrl.getParentId()).orElseThrow(()
                ->new NoSuchElementException("URL을 찾지 못했습니다."));
        InspectionUrl newChildUrl = childUrl.toEntity(parent, member);

        InspectionUrl savedUrl = inspectionUrlRepository.save(newChildUrl);

        // then
        assertThat(savedUrl.getParent().getId()).isEqualTo(parent.getId());
    }
}
