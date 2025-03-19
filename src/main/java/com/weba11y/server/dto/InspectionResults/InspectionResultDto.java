package com.weba11y.server.dto.InspectionResults;

import com.weba11y.server.domain.enums.InspectionItems;
import lombok.*;


@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class InspectionResultDto {

    private InspectionItems inspectionItems;

    private String summary;

    private String codeLine;

}
