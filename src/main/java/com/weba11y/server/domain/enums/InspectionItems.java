package com.weba11y.server.domain.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static com.weba11y.server.domain.enums.AssessmentLevel.*;
import static com.weba11y.server.domain.enums.Importance.*;

@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)

public enum InspectionItems {
    // 항목 명 , 항목 번호

    // 대체 텍스트, 0
    ALT_TEXT(0, "대체 텍스트", A, MODERATE),
    ALT_MULTIMEDIA(1, "멀티미디어 대체수단", AAA, CRITICAL)
    // 항목 추가
    ;
    private final int number;
    private final String name;
    private final AssessmentLevel assessmentLevel;
    private final Importance importance;
}
