package com.weba11y.server.api.dto;


import com.weba11y.server.api.dto.accessibilityViolation.AccessibilityViolationDto;
import com.weba11y.server.api.dto.inspectionSummary.InspectionSummaryDto;
import com.weba11y.server.api.dto.inspectionUrl.InspectionUrlDto;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class InspectionDetailDto {

    private InspectionUrlDto.Response inspectionUrl;

    @Builder.Default
    private List<InspectionSummaryDto.InspectionSummaryMetadataDto> inspectionSummaries = new ArrayList<>();

    private InspectionSummaryDto latestInspectionSummary;

    @Builder.Default
    private List<AccessibilityViolationDto> top5Violations = new ArrayList<>();

}
