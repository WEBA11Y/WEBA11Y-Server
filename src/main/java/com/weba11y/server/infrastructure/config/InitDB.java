package com.weba11y.server.infrastructure.config;

import com.weba11y.server.domain.violation.AccessibilityViolation;
import com.weba11y.server.domain.inspection.summary.InspectionSummary;
import com.weba11y.server.domain.inspection.url.InspectionUrl;
import com.weba11y.server.domain.member.Member;
import com.weba11y.server.domain.enums.InspectionItems;
import com.weba11y.server.api.dto.member.JoinDto;
import com.weba11y.server.infrastructure.persistence.AccessibilityViolationRepository;
import com.weba11y.server.infrastructure.persistence.InspectionSummaryRepository;
import com.weba11y.server.infrastructure.persistence.InspectionUrlRepository;
import com.weba11y.server.application.service.AuthService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

//@Component
@RequiredArgsConstructor
@Profile("!test")
public class InitDB {
    private final InitService initService;

   // @PostConstruct
    public void init() {
        initService.initMember();
        initService.initInspectionUrl();
        initService.initInspectionResult();
    }
}

//@Component
@RequiredArgsConstructor
class InitService { // 외부 클래스로 변경
    private final AuthService authService;
    private final InspectionUrlRepository inspectionUrlRepository;
    private final InspectionSummaryRepository summaryRepository;
    private final AccessibilityViolationRepository accessibilityViolationRepository;
    private Long memberId;

    // 회원 생성
    @Transactional
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
    @Transactional
    public void initInspectionUrl() {
        // Member
        Member member = authService.retrieveMember(memberId);
        // 부모 URL 생성
        for (int i = 1; i <= 3; i++) {
            InspectionUrl parentUrl = InspectionUrl.builder()
                    .description("ParentURL : " + i)
                    .url("https://www.parent" + i + ".com")
                    .member(member)
                    .build();
            InspectionUrl savedParentUrl = saveUrl(parentUrl); // 부모 ID 저장
            // 자식 URL 생성
            for (int j = 1; j <= 3; j++) {
                InspectionUrl childUrl = InspectionUrl.builder()
                        .description("ChildURL : " + j)
                        .url("https://www.parent" + i + ".com/child" + j)
                        .member(member)
                        .build();
                childUrl.addParentUrl(parentUrl);
                saveUrl(childUrl);
            }
        }
        for (int i = 1; i <= 10; i++) {
            InspectionUrl testUrl = InspectionUrl.builder()
                    .description("TestURL : " + i)
                    .url("https://www.test" + i + ".com")
                    .member(member)
                    .build();
            saveUrl(testUrl);
        }
    }
    @Transactional
    public void initInspectionResult() {
        InspectionUrl inspectionUrl = inspectionUrlRepository.findByUrlId(1L).orElseThrow(
                () -> new RuntimeException("InitDB 생성 중 실패"));

        InspectionSummary inspectionSummary = summaryRepository.save(InspectionSummary.builder()
                .inspectionUrl(inspectionUrl)
                .build());

        inspectionUrl.addSummary(inspectionSummary);

        for (int i = 0; i < 10; i++) {
            AccessibilityViolation result = AccessibilityViolation.builder()
                    .inspectionSummary(inspectionSummary)
                    .description("Test : " + i)
                    .inspectionItem(InspectionItems.ALT_TEXT)
                    .codeLine("<div>Test : " + i + " </div>")
                    .build();

            inspectionSummary.addViolation(accessibilityViolationRepository.save(result));
        }
        for (int i = 10; i < 20; i++) {
            AccessibilityViolation result = AccessibilityViolation.builder()
                    .inspectionSummary(inspectionSummary)
                    .description("Test : " + i)
                    .inspectionItem(InspectionItems.AUTO_PLAY)
                    .codeLine("<div>Test : " + i + " </div>")
                    .build();

            inspectionSummary.addViolation(accessibilityViolationRepository.save(result));
        }
        for (int i = 20; i < 30; i++) {
            AccessibilityViolation result = AccessibilityViolation.builder()
                    .inspectionSummary(inspectionSummary)
                    .description("Test : " + i)
                    .inspectionItem(InspectionItems.PAGE_TITLES)
                    .codeLine("<div>Test : " + i + " </div>")
                    .build();

            inspectionSummary.addViolation(accessibilityViolationRepository.save(result));
        }
    }

    private InspectionUrl saveUrl(InspectionUrl inspectionUrl) {
        return inspectionUrlRepository.save(inspectionUrl);
    }
}
