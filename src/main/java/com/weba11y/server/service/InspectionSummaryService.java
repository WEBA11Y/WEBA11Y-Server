package com.weba11y.server.service;

import com.weba11y.server.domain.InspectionSummary;
import com.weba11y.server.domain.InspectionUrl;
import com.weba11y.server.dto.inspectionSummary.InspectionSummaryDto;

import java.util.List;

public interface InspectionSummaryService {

    InspectionSummary save(InspectionUrl inspectionUrl);

    List<InspectionSummaryDto> retrieveAllByUrlIdAndMemberId(Long urlId, Long memberID);


    List<InspectionSummaryDto.InspectionSummaryMetadataDto> retrieveSummariesMetadataByUrlAndMember(Long urlId, Long memberId);

    InspectionSummaryDto retrieveByIdAndUrlIdAndMemberId(Long inspectionSummaryId, Long urlId, Long memberId);

    InspectionSummaryDto retrieveLatestInspectionSummaryByUrlIdAndMemberId(Long urlId, Long memberId);

}
