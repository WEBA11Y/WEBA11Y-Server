package com.weba11y.server.application.service;

import com.weba11y.server.domain.inspection.summary.InspectionSummary;
import com.weba11y.server.domain.inspection.url.InspectionUrl;
import com.weba11y.server.api.dto.inspectionSummary.InspectionSummaryDto;

import java.util.List;

public interface InspectionSummaryService {

    InspectionSummary save(InspectionUrl inspectionUrl);

    List<InspectionSummaryDto.InspectionSummaryMetadataDto> retrieveSummariesMetadataByUrlAndMember(Long urlId, Long memberId);

    InspectionSummaryDto retrieveLatestInspectionSummaryByUrlIdAndMemberId(Long urlId, Long memberId);

}
