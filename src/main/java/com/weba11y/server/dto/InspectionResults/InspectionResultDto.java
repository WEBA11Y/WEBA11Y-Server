package com.weba11y.server.dto.InspectionResults;

import com.weba11y.server.domain.InspectionResult;
import com.weba11y.server.domain.InspectionResultReactive;
import com.weba11y.server.domain.InspectionUrl;
import com.weba11y.server.domain.enums.AssessmentLevel;
import com.weba11y.server.domain.enums.Importance;
import com.weba11y.server.domain.enums.InspectionItems;
import lombok.*;
import org.jsoup.nodes.Element;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class InspectionResultDto {

    private Long id;

    private InspectionItems inspectionItems;

    private AssessmentLevel assessmentLevel;

    private Importance importance;

    private String summary;

    private String codeLine;

    private LocalDateTime createDate;

    private LocalDateTime updateDate;


    public static InspectionResultDto createInspectionResultDto(Element element, InspectionItems inspectionItems) {
        String codeLine = element.outerHtml();
        if (codeLine.length() > 255) {
            codeLine = codeLine.substring(0, 255); // 255자까지 자르기
        }

        return InspectionResultDto.builder()
                .inspectionItems(inspectionItems)
                .assessmentLevel(inspectionItems.getAssessmentLevel())
                .importance(inspectionItems.getImportance())
                .summary(inspectionItems.getSummary())
                .codeLine(codeLine)
                .createDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .build();
    }

    public InspectionResult toEntity(InspectionUrl inspectionUrl) {
        return InspectionResult.builder()
                .inspectionUrl(inspectionUrl)
                .inspectionItems(this.inspectionItems)
                .summary(this.summary)
                .codeLine(this.codeLine)
                .build();
    }

    public InspectionResultReactive toReactiveEntity(Long inspectionUrlId) {
        return InspectionResultReactive.builder()
                .inspectionUrlId(inspectionUrlId)
                .inspectionItems(this.inspectionItems.getNumber())
                .summary(this.summary)
                .codeLine(this.codeLine)
                .createDate(this.createDate.toString())
                .updateDate(this.updateDate.toString())
                .build();
    }

    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    public static class ResultListResponse {
        @Builder.Default
        private List<InspectionResultDto> content = new ArrayList<>();
        private int totalPage;
        private int currentPage;
    }
}
