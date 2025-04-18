package com.weba11y.server.service.implement;

import com.weba11y.server.domain.InspectionResult;
import com.weba11y.server.domain.enums.AssessmentLevel;
import com.weba11y.server.domain.enums.Importance;
import com.weba11y.server.domain.enums.InspectionItems;
import com.weba11y.server.dto.InspectionResults.InspectionResultDto;
import com.weba11y.server.jpa.repository.InspectionResultRepository;
import com.weba11y.server.service.InspectionResultService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
@Transactional(value = "transactionManager", readOnly = true)
@RequiredArgsConstructor
public class InspectionResultServiceImpl implements InspectionResultService {

    private final InspectionResultRepository inspectionResultRepository;


    @Value("${page.result.size}")
    private int size;


    @Override
    public List<LocalDate> retrieveInspectionResultDateByUrlId(Long urlId) {
        try {
            return Optional.ofNullable(inspectionResultRepository.findCreateDatesByInspectionUrlId(urlId))
                    .orElseGet(Collections::emptyList);
        } catch (Exception e) {
            log.error("기록 날짜 가져오는 중 오류 발생 - urlId: {}, error: ", urlId, e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<InspectionResultDto> retrieveResultsByUrlIdAndDate(Long urlId, LocalDate date) {
        return inspectionResultRepository.findInspectionResultsByUrlIdAndCreateDate(urlId, date)
                .stream()
                .map(InspectionResult::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public InspectionResultDto.ResultListResponse retrieveResultsByDateAndImportance(int page, Long urlId, LocalDate date, Importance importance) {
        return retrieveResults(page, urlId, date, InspectionItems.findItemsByImportance(importance));
    }

    @Override
    public InspectionResultDto.ResultListResponse retrieveResultsByDateAndLevel(int page, Long urlId, LocalDate date, AssessmentLevel assessmentLevel) {
        return retrieveResults(page, urlId, date, InspectionItems.findItemsByAssessmentLevel(assessmentLevel));
    }


    private InspectionResultDto.ResultListResponse retrieveResults(int page, Long urlId, LocalDate date, List<InspectionItems> items) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<InspectionResult> list = inspectionResultRepository.findByUrlIdAndDateAndItems(pageable, urlId, date, items);
        return InspectionResultDto.ResultListResponse.builder()
                .content(list.stream().map(InspectionResult::toDto).collect(Collectors.toList()))
                .totalPage(list.getTotalPages())
                .currentPage(page)
                .build();
    }
}
