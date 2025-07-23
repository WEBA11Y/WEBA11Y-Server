package com.weba11y.server.domain.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.weba11y.server.domain.enums.AssessmentLevel.*;
import static com.weba11y.server.domain.enums.Importance.*;

@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public enum InspectionItems {
    // 항목 번호, 항목 명, 평가 수준, 중요성, 검사 대상 태그 리스트, 요약
    ALT_TEXT(0, "대체 텍스트", A, CRITICAL, createTags("img", "area", "input", "object", "applet", "iframe"), "대체 텍스트가 제공되지 않았습니다."),
    ALT_MULTIMEDIA(1, "멀티미디어 대체수단", A, CRITICAL, createTags("video", "audio", "object"), "멀티미디어 대체수단이 제공되지 않았습니다."),
    TABLE_STRUCTURE(2, "표의 구성", A, SERIOUS, createTags("table"), "표는 이해하기 쉽게 구성되어야 합니다."),
    LINEAR_STRUCTURE(3, "콘텐츠의 선형 구조", A, MODERATE, createTags("div", "span"), "콘텐츠는 논리적인 순서로 제공되어야 합니다."),
    CLEAR_INSTRUCTIONS(4, "명확한 지시사항 제공", A, MODERATE, createTags("form"), "지시사항은 인식될 수 있어야 합니다."),
    COLOR_INDEPENDENCE(5, "색에 무관한 콘텐츠 인식", A, MODERATE, createTags("div", "span"), "콘텐츠는 색에 관계없이 인식될 수 있어야 합니다."),
    AUTO_PLAY(6, "자동 재생 금지", A, SERIOUS, createTags("video", "audio"), "자동으로 소리가 재생되지 않아야 합니다."),
    TEXT_CONTRAST(7, "텍스트 콘텐츠의 명도 대비", AA, SERIOUS, createTags("p", "span"), "텍스트와 배경 간의 명도 대비는 4.5:1 이상이어야 합니다."),
    CONTENT_SEPARATION(8, "콘텐츠 간의 구분", A, MODERATE, createTags("div", "section"), "이웃한 콘텐츠는 구별될 수 있어야 합니다."),
    KEYBOARD_ACCESSIBILITY(9, "키보드 접근성 보장", A, SERIOUS, createTags("a", "button", "input"), "모든 기능은 키보드만으로 사용할 수 있어야 합니다."),
    FOCUS_INDICATION(10, "포커스 표시", A, SERIOUS, createTags("a", "button", "input"), "키보드에 의한 초점은 논리적으로 이동해야 하며, 시각적으로 구별할 수 있어야 합니다."),
    USER_CONTROL(11, "사용자 제어 제공", A, SERIOUS, createTags("video", "audio"), "자동으로 변경되는 콘텐츠는 제어할 수 있어야 합니다."),
    SKIP_REPEATED_CONTENT(12, "반복 영역 건너뛰기", A, MODERATE, createTags("nav"), "콘텐츠의 반복되는 영역은 건너뛸 수 있어야 합니다."),
    PAGE_TITLES(13, "페이지 제목 제공", A, MODERATE, createTags("title"), "페이지, 프레임, 콘텐츠 블록에는 적절한 제목을 제공해야 합니다."),
    LINK_TEXT(14, "적절한 링크 텍스트 제공", A, MODERATE, createTags("a"), "링크 텍스트는 용도나 목적을 이해할 수 있도록 제공해야 합니다."),
    ERROR_IDENTIFICATION(15, "입력 오류 식별", A, SERIOUS, createTags("input", "form"), "입력 오류를 식별할 수 있어야 합니다."),
    ERROR_CORRECTION(16, "입력 오류 정정 제공", AA, SERIOUS, createTags("input", "form"), "입력 오류를 정정할 수 있는 방법을 제공해야 합니다."),
    BASIC_LANGUAGE(17, "기본 언어 표시", A, MODERATE, createTags("html"), "주로 사용하는 언어를 명시해야 합니다."),
    USER_FRIENDLY_EXECUTION(18, "사용자 요구에 따른 실행", A, MODERATE, createTags("a", "button"), "사용자가 의도하지 않은 기능은 실행되지 않아야 합니다."),
    HELP_INFORMATION(19, "찾기 쉬운 도움 정보", AA, MODERATE, createTags("help"), "도움 정보는 동일한 순서로 접근할 수 있어야 합니다."),
    REPEATED_INPUT(20, "반복 입력 정보 자동화", A, MINOR, createTags("input"), "반복되는 입력 정보는 자동 입력할 수 있어야 합니다."),
    ACCESSIBLE_AUTHENTICATION(21, "접근 가능한 인증", A, SERIOUS, createTags("input"), "인증 과정은 인지 기능 테스트에만 의존해서는 안 된다."),
    MARKUP_ERROR_PREVENTION(22, "마크업 오류 방지", A, SERIOUS, createTags("html"), "마크업 언어의 요소는 오류가 없어야 한다."),
    WEB_APP_ACCESSIBILITY(23, "웹 애플리케이션 접근성 준수", A, CRITICAL, createTags("application"), "웹 애플리케이션은 접근성이 있어야 한다."),
    USER_INTERFACE_LABELS(24, "레이블 제공", A, SERIOUS, createTags("label"), "사용자 인터페이스 구성요소에는 대응하는 레이블을 제공해야 한다."),
    ERROR_MESSAGES(25, "명확한 오류 메시지 제공", A, SERIOUS, createTags("input", "form"), "명확한 오류 메시지를 제공해야 한다."),
    CONTENT_ORDER(26, "페이지 내 콘텐츠 순서 제공", A, MODERATE, createTags("div", "ol", "ul"), "페이지 내 콘텐츠 순서가 필요합니다."),
    INTERACTIVE_ELEMENT_STATE(27, "상호작용 요소의 상태 표시", A, MODERATE, createTags("input", "button"), "상호작용 요소의 상태 표시가 필요합니다."),
    SCREEN_READER_COMPATIBILITY(28, "스크린 리더 호환성", A, CRITICAL, createTags("div", "span"), "스크린 리더에 대한 호환성이 필요합니다."),
    CUSTOMIZABLE_CONTENT(29, "사용자 정의 가능한 콘텐츠", A, MINOR, createTags("div", "span"), "사용자 정의 가능한 콘텐츠가 필요합니다."),
    SIMILAR_CONTENT_DIFFERENTIATION(30, "비슷한 콘텐츠의 차별화", A, MODERATE, createTags("div", "p"), "비슷한 콘텐츠의 차별화가 필요합니다."),
    ALL_CONTENT_ACCESSIBILITY(31, "모든 콘텐츠의 접근성", A, CRITICAL, createTags("*"), "모든 콘텐츠의 접근성이 필요합니다."),
    MULTI_POINTER_INPUT(32, "단일 포인터 입력 지원", A, SERIOUS, createTags("pointer"), "다중 포인터 입력은 단일 포인터 입력으로 조작할 수 있어야 합니다."),
    TIME_LIMIT_ADJUSTMENT(33, "응답시간 조절", A, MODERATE, createTags("timer"), "시간제한이 있는 콘텐츠는 응답시간을 조절할 수 있어야 합니다.");

    private final int number;
    private final String name;
    private final AssessmentLevel assessmentLevel;
    private final Importance importance;
    private final List<String> targetTags;
    private final String description;

    private static List<String> createTags(String... tags) {
        return List.of(tags);
    }

    public static InspectionItems findByNumber(int number) {
        for (InspectionItems item : InspectionItems.values()) {
            if (item.getNumber() == number) {
                return item;
            }
        }
        throw new IllegalArgumentException("No enum constant with number " + number);
    }

    public static List<InspectionItems> findItemsByAssessmentLevel(String assessmentLevel) {
        return Arrays.stream(InspectionItems.values())
                .filter(item -> item.getAssessmentLevel().name().equals(assessmentLevel.toUpperCase()))
                .collect(Collectors.toList());
    }

    public static List<InspectionItems> findItemsByImportance(String importance) {
        return Arrays.stream(InspectionItems.values())
                .filter(item -> item.getImportance().name().equals(importance.toUpperCase()))
                .collect(Collectors.toList());
    }
}
