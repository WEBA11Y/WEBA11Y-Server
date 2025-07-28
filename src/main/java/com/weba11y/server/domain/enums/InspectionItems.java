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
    // 번호, 항목명, WCAG 레벨, 중요도, 검사 대상 태그, 설명

    // --- 인식의 용이성 (Perceivable) - 9개 ---
    ALT_TEXT(1, "대체 텍스트 제공", A, CRITICAL, createTags("img", "area", "input", "object", "iframe"),
            "텍스트 아닌 콘텐츠는 의미나 용도를 인식할 수 있도록 대체 텍스트를 제공해야 합니다."),
    MULTIMEDIA_ALTERNATIVE(2, "멀티미디어 대체수단", A, CRITICAL, createTags("video", "audio", "object"),
            "멀티미디어 콘텐츠에는 자막, 대본 또는 수어를 제공해야 합니다."),
    TABLE_STRUCTURE(3, "표의 구성", A, SERIOUS, createTags("table"),
            "표는 이해하기 쉽게 제목 셀과 데이터 셀을 구분해 구성해야 합니다."),
    LINEAR_STRUCTURE(4, "콘텐츠의 선형 구조", A, MODERATE, createTags("div", "span", "section"),
            "콘텐츠는 보조기술 사용자가 이해할 수 있도록 논리적인 순서로 제공해야 합니다."),
    CLEAR_INSTRUCTIONS(5, "명확한 지시사항", A, MODERATE, createTags("form", "button", "input"),
            "지시사항은 모양, 위치, 색 등에 의존하지 않고 인식 가능해야 합니다."),
    COLOR_INDEPENDENCE(6, "색에 무관한 콘텐츠 인식", A, MODERATE, createTags("div", "span", "canvas"),
            "콘텐츠는 색만으로 정보를 구별하지 않도록 제공해야 합니다."),
    AUTO_PLAY(7, "자동 재생 금지", A, SERIOUS, createTags("video", "audio"),
            "자동으로 3초 이상 소리가 재생되지 않아야 하며, 제어 수단을 제공해야 합니다."),
    TEXT_CONTRAST(8, "텍스트 대비", AA, SERIOUS, createTags("p", "span", "label"),
            "텍스트와 배경 간의 명도 대비는 4.5:1 이상이어야 합니다."),
    CONTENT_SEPARATION(9, "콘텐츠 구분", A, MODERATE, createTags("div", "section", "article"),
            "이웃한 콘텐츠는 테두리, 간격 등으로 시각적으로 구별되어야 합니다."),

    // --- 운용의 용이성 (Operable) - 15개 ---
    KEYBOARD_ACCESSIBILITY(10, "키보드 사용 보장", A, CRITICAL, createTags("a", "button", "input"),
            "모든 기능은 키보드만으로도 사용 가능해야 합니다."),
    FOCUS_INDICATION(11, "초점 이동과 표시", A, SERIOUS, createTags("a", "button", "input"),
            "키보드 초점은 논리적 순서로 이동하며, 시각적으로 구별 가능해야 합니다."),
    OPERABLE_CONTROLS(12, "조작 가능한 콘트롤", A, SERIOUS, createTags("a", "button", "input"),
            "사용자 입력 및 콘트롤은 조작 가능하도록 충분한 크기와 영역을 제공해야 합니다."),
    CHARACTER_SHORTCUT(13, "문자 단축키 오류 방지", A, SERIOUS, createTags("*"),
            "단일 문자 단축키는 비활성화, 재설정, 또는 초점 시에만 활성화해야 합니다."),
    TIME_LIMIT_ADJUSTMENT(14, "응답시간 조절", A, MODERATE, createTags("timer", "form"),
            "시간제한 콘텐츠는 해제 또는 연장할 수 있는 방법을 제공해야 합니다."),
    USER_CONTROL(15, "자동 변경 콘텐츠 제어", A, SERIOUS, createTags("marquee", "video", "audio"),
            "자동으로 변경되는 콘텐츠는 일시 정지, 정지, 탐색 기능을 제공해야 합니다."),
    FLASH_PREVENTION(16, "깜빡임/번쩍임 제한", A, SERIOUS, createTags("div", "canvas"),
            "초당 3~50회 주기의 깜빡이거나 번쩍이는 콘텐츠를 제공하지 않아야 합니다."),
    SKIP_REPEATED_CONTENT(17, "반복 영역 건너뛰기", A, MODERATE, createTags("nav"),
            "반복되는 메뉴나 링크 영역은 건너뛸 수 있어야 합니다."),
    PAGE_TITLES(18, "페이지/프레임 제목", A, MODERATE, createTags("title", "iframe"),
            "페이지와 프레임에는 적절한 제목을 제공해야 합니다."),
    LINK_TEXT(19, "링크 텍스트 명확성", A, MODERATE, createTags("a"),
            "링크 텍스트는 용도나 목적을 이해할 수 있도록 제공해야 합니다."),
    FIXED_REFERENCE_INFO(20, "고정된 참조 위치 정보", A, MODERATE, createTags("section", "article"),
            "전자출판문서 형식의 페이지는 일관된 참조 위치 정보를 제공해야 합니다."),
    SINGLE_POINTER_INPUT(21, "단일 포인터 입력", A, SERIOUS, createTags("canvas", "pointer"),
            "다중 포인터/경로 기반 입력은 단일 포인터 입력으로 조작할 수 있어야 합니다."),
    POINTER_INPUT_CANCELLATION(22, "포인터 입력 취소", A, SERIOUS, createTags("button", "canvas"),
            "단일 포인터 입력으로 실행되는 기능은 취소할 수 있어야 합니다."),
    LABEL_AND_NAME(23, "레이블과 네임 일치", A, SERIOUS, createTags("label", "input", "button"),
            "시각적 레이블을 가진 UI 구성요소는 접근성 네임에 해당 텍스트를 포함해야 합니다."),
    MOTION_ACTIVATION(24, "동작기반 작동 제어", A, SERIOUS, createTags("device-motion", "sensor"),
            "동작기반 작동 기능은 비활성화하거나 UI로 대체 조작할 수 있어야 합니다."),

    // --- 이해의 용이성 (Understandable) - 5개 ---
    READABILITY(25, "가독성", A, MODERATE, createTags("html", "body"),
            "페이지의 주언어(lang 속성)를 지정하고 가독성을 확보해야 합니다."),
    PREDICTABILITY(26, "예측 가능성", A, MODERATE, createTags("a", "button", "form"),
            "사용자가 의도하지 않은 맥락 변화나 기능 실행이 일어나지 않아야 합니다."),
    ERROR_IDENTIFICATION(27, "입력 오류 식별", A, SERIOUS, createTags("input", "form"),
            "사용자가 입력 오류를 쉽게 식별할 수 있어야 합니다."),
    ERROR_CORRECTION(28, "입력 오류 정정", AA, SERIOUS, createTags("input", "form"),
            "입력 오류를 정정할 수 있는 수단(재입력, 힌트 등)을 제공해야 합니다."),
    HELP_INFORMATION(29, "도움 정보 접근성", AA, MODERATE, createTags("help", "aside"),
            "도움말 정보는 일관된 위치와 순서로 접근할 수 있어야 합니다."),

    // --- 견고성 (Robust) - 4개 ---
    MARKUP_VALIDITY(30, "문법 준수", A, SERIOUS, createTags("html"),
            "마크업 언어는 오류 없이 작성되어야 합니다."),
    WEB_APP_ACCESSIBILITY(31, "웹 애플리케이션 접근성", A, CRITICAL, createTags("application", "div"),
            "웹 애플리케이션은 접근성을 준수해야 합니다."),
    SCREEN_READER_COMPATIBILITY(32, "스크린 리더 호환성", A, CRITICAL, createTags("div", "span", "aria-*"),
            "보조기술(스크린 리더)과 호환되도록 WAI-ARIA 속성을 준수해야 합니다."),
    UI_LABELS(33, "사용자 인터페이스 레이블", A, SERIOUS, createTags("label", "input", "button"),
            "모든 UI 구성요소는 대응하는 레이블을 제공해야 합니다.");

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
        return Arrays.stream(values())
                .filter(item -> item.getNumber() == number)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No enum constant with number " + number));
    }

    public static List<InspectionItems> findItemsByAssessmentLevel(String level) {
        return Arrays.stream(values())
                .filter(item -> item.getAssessmentLevel().name().equalsIgnoreCase(level))
                .collect(Collectors.toList());
    }

    public static List<InspectionItems> findItemsByImportance(String level) {
        return Arrays.stream(values())
                .filter(item -> item.getImportance().name().equalsIgnoreCase(level))
                .collect(Collectors.toList());
    }
}