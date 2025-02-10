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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

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

    private static final String URL_REGEX = "^(https?://)(www\\.)?([a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}(/.*)?$";

    private static final Pattern URL_PATTERN = Pattern.compile(URL_REGEX);

    @BeforeEach
    void beforeEach() {
        // Member 생성
        Member newMember = Member.builder()
                .userId("test")
                .password("test1234")
                .name("test")
                .birthday(LocalDate.now())
                .phoneNum("01011332244")
                .build();

        member = memberRepository.save(newMember);


        // parent URL 생성
        InspectionUrl parent = InspectionUrl.builder()
                .summary("Test")
                .url("https://www.test.com")
                .member(newMember)
                .build();

        parentUrl = inspectionUrlRepository.save(parent);

        // Child URL 생성
        for (int i = 0; i < 5; i++) {
            InspectionUrl childUrl = InspectionUrl.builder()
                    .url("https://www.test.com/child/" + i)
                    .summary("Child " + i)
                    .member(member)
                    .build();
            childUrl.addParentUrl(parentUrl);
            inspectionUrlRepository.save(childUrl);
            parentUrl.addChildUrl(childUrl);
        }
    }

    @Test
    @DisplayName("URL 등록")
    void URL_등록() {
        // given
        InspectionUrlRequestDto newUrlDto = InspectionUrlRequestDto.builder()
                .summary("Naver")
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
                .summary("Naver")
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
                .summary("Naver")
                .url("https://www.naver.com")
                .parentId(parentUrl.getId())
                .build();
        // when
        InspectionUrl parent = inspectionUrlRepository.findById(childUrl.getParentId()).orElseThrow(()
                -> new NoSuchElementException("URL을 찾지 못했습니다."));
        InspectionUrl newChildUrl = childUrl.toEntity(parent, member);

        InspectionUrl savedUrl = inspectionUrlRepository.save(newChildUrl);

        // then
        assertThat(savedUrl.getParent().getId()).isEqualTo(parent.getId());
    }

    @Test
    @DisplayName("모든 URL 가져오기")
    void 모든_URL_가져오기() {
        // given
        Long memberId = member.getId();
        // when
        List<InspectionUrl> urls = inspectionUrlRepository.findAllByMemberId(memberId);
        // then
        assertThat(6).isEqualTo(urls.size());
    }

    @Test
    @DisplayName("URL 조회")
    void URL_조회() {
        // given
        Long memberId = member.getId();
        Long urlId = parentUrl.getId();
        // when
        InspectionUrl findUrl = inspectionUrlRepository.findByIdAndMemberId(urlId, memberId).orElseThrow(
                () -> new NoSuchElementException("해당 URL을 찾을 수 없습니다.")
        );
        // then
        assertThat(parentUrl.getId()).isEqualTo(findUrl.getId());
        assertThat(parentUrl.getChild().size()).isEqualTo(findUrl.getChild().size());
    }

    @Test
    @DisplayName("자식 URL 가져오기")
    void 자식_URL_가져오기() {
        // given
        Long memberId = member.getId();
        Long parentId = parentUrl.getId();
        // when
        List<InspectionUrl> childUrls = inspectionUrlRepository.findAllByMemberIdAndParentId(memberId, parentId);
        // then
        assertThat(5).isEqualTo(childUrls.size());
    }


    @Test
    @DisplayName("올바른 URL인지 검증")
    void URL_검증() {
        // given
        String url = "https://www.naver.com";
        String url2 = "https://youtube.com";
        String url3 = "www.youtube.com";
        String url4 = "youtube.com";

        // when
        boolean result = URL_PATTERN.matcher(url).matches();
        boolean result2 = URL_PATTERN.matcher(url2).matches();
        boolean result3 = URL_PATTERN.matcher(url3).matches();
        boolean result4 = URL_PATTERN.matcher(url4).matches();
        // then
        assertTrue(result);
        assertTrue(result2);
        assertFalse( result3);
        assertFalse(result4);
    }

    @Test
    @DisplayName("실제로 존재하는 URL인지 검증")
    void URL_검증2() {
        // given
        String url = "https://www.naver.com";
        String url2 = "https://test.com";
        String url3 = "https://youtube.com";
        // when
        boolean result = doesUrlExist(url);
        boolean result2 = doesUrlExist(url2);
        boolean result3 = doesUrlExist(url3);
        // then
        assertTrue(result);
        assertFalse(result2);
        assertTrue(result3);
    }

    private boolean doesUrlExist(String url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("HEAD"); // HEAD 요청을 보내어 응답을 확인
            connection.setConnectTimeout(5000); // 연결 타임아웃 설정
            connection.setReadTimeout(5000); // 읽기 타임아웃 설정
            connection.connect();

            int responseCode = connection.getResponseCode();
            return (responseCode >= 200 && responseCode < 400); // 200~399 응답 코드는 유효한 URL
        } catch (IOException e) {
            return false; // 예외 발생 시 URL이 존재하지 않음
        }
    }
}
