package com.weba11y.server.configuration;

import com.weba11y.server.domain.Member;
import com.weba11y.server.dto.InspectionUrl.InspectionUrlRequestDto;
import com.weba11y.server.dto.member.JoinDto;
import com.weba11y.server.service.AuthService;
import com.weba11y.server.service.InspectionUrlService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class InitDB {
    private final InitService initService;

    @PostConstruct
    public void init() {
        initService.initMember();
        initService.initInspectionUrl();
    }
}

@Component
@RequiredArgsConstructor
class InitService { // 외부 클래스로 변경
    private final AuthService authService;
    private final InspectionUrlService inspectionUrlService;
    private Long memberId;

    // 회원 생성
    public void initMember() {
        JoinDto joinDto = JoinDto.builder()
                .userId("test1234")
                .password("test1234")
                .name("Test")
                .phoneNum("01011112222")
                .birthday(LocalDate.now())
                .build();
        memberId = authService.join(joinDto).getId();
    }

    public void initInspectionUrl() {
        // Member
        Member member = authService.retrieveMember(memberId);
        // 부모 URL 생성
        for (int i = 1; i <= 3; i++) {
            InspectionUrlRequestDto parentUrlDto = InspectionUrlRequestDto.builder()
                    .summary("ParentURL : " + i)
                    .url("https://www.parent" + i + ".com")
                    .build();
            Long parentId = inspectionUrlService.saveUrl(parentUrlDto, member).getId(); // 부모 ID 저장
            // 자식 URL 생성
            for (int j = 1; j <= 3; j++) {
                InspectionUrlRequestDto childUrlDto = InspectionUrlRequestDto.builder()
                        .summary("ChildURL : " + j)
                        .url("https://www.parent" + i + ".com/child" + j)
                        .parentId(parentId) // 부모 ID 사용
                        .build();
                inspectionUrlService.saveUrl(childUrlDto, member);
            }

        }

        InspectionUrlRequestDto childUrlDto = InspectionUrlRequestDto.builder()
                .summary("ChildURL : " + 1234)
                .url("https://www.parent" + 4123 + ".com/child" + 1234)
                .parentId(3L) // 부모 ID 사용
                .build();
        inspectionUrlService.saveUrl(childUrlDto, member);
    }
}
