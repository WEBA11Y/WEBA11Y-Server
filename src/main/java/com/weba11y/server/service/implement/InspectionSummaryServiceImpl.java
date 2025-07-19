package com.weba11y.server.service.implement;

import com.weba11y.server.domain.InspectionSummary;
import com.weba11y.server.domain.InspectionUrl;
import com.weba11y.server.dto.inspectionSummary.InspectionSummaryDto;
import com.weba11y.server.repository.InspectionSummaryRepository;
import com.weba11y.server.service.InspectionSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InspectionSummaryServiceImpl implements InspectionSummaryService {

    private final InspectionSummaryRepository inspectionSummaryRepository;

    @Override
    @Transactional
    public InspectionSummary save(InspectionUrl inspectionUrl) {
        InspectionSummary inspectionSummary = InspectionSummary.builder()
                .inspectionUrl(inspectionUrl)
                .build();
        try {
            return inspectionSummaryRepository.save(inspectionSummary);
        } catch (Exception e) {
            throw new RuntimeException("검사 요약 정보를 생성하던 중 오류가 발생했습니다.");
        }
    }

    @Override
    public List<InspectionSummaryDto> retrieveAllByUrlIdAndMemberId(Long urlId, Long memberID) {
        return null;
    }

    @Override
    public List<InspectionSummaryDto.InspectionSummaryMetadataDto> retrieveSummariesMetadataByUrlAndMember(Long urlId, Long memberId) {
        return inspectionSummaryRepository.findAllByUrlIdAndMemberId(urlId, memberId)
                .stream()
                .map(InspectionSummary::toMetadataDto)
                .collect(Collectors.toList());
    }

    @Override
    public InspectionSummaryDto retrieveByIdAndUrlIdAndMemberId(Long inspectionSummaryId, Long urlId, Long memberId) {
        return null;
    }

    @Override
    public InspectionSummaryDto retrieveLatestInspectionSummaryByUrlIdAndMemberId(Long urlId, Long memberId) {
        return inspectionSummaryRepository.findLatestByUrlIdAndMemberId(urlId, memberId).orElseThrow(
                () -> new NoSuchElementException("Latest InspectionSummary Not Found")
        ).toDto();
    }

}
