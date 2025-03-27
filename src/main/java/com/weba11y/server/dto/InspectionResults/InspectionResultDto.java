package com.weba11y.server.dto.InspectionResults;

import com.weba11y.server.domain.InspectionResult;
import com.weba11y.server.domain.InspectionResultReactive;
import com.weba11y.server.domain.InspectionUrl;
import com.weba11y.server.domain.enums.InspectionItems;
import lombok.*;
import org.jsoup.nodes.Element;


@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class InspectionResultDto {

    private InspectionItems inspectionItems;

    private String summary;

    private String codeLine;



    public static InspectionResultDto createInspectionResultDto(Element element, InspectionItems inspectionItems){
        return InspectionResultDto.builder()
                .inspectionItems(inspectionItems)
                .summary(inspectionItems.getSummary())
                .codeLine(element.outerHtml())
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
                .build();
    }

}
