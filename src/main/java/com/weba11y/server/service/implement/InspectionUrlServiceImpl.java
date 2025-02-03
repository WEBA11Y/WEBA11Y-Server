package com.weba11y.server.service.implement;

import com.weba11y.server.domain.InspectionUrl;
import com.weba11y.server.domain.Member;
import com.weba11y.server.dto.InspectionUrl.InspectionUrlRequestDto;
import com.weba11y.server.dto.InspectionUrl.InspectionUrlResponseDto;
import com.weba11y.server.exception.custom.DuplicateFieldException;
import com.weba11y.server.repository.InspectionUrlRepository;
import com.weba11y.server.service.InspectionUrlService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class InspectionUrlServiceImpl implements InspectionUrlService {
    private final InspectionUrlRepository repository;

    @Transactional
    @Override
    public InspectionUrlResponseDto saveUrl(InspectionUrlRequestDto dto, Member member) {
        // 이미 등록된 URL 인지 확인
        isExistsInspectionUrl(dto.getUrl(), member.getId());
        try {
            InspectionUrl newUrl;
            // 부모 URL 유무
            if (dto.getParentId() != null && dto.getParentId() > 0) {
                InspectionUrl parentUrl = retrieveById(dto.getParentId());
                newUrl = dto.toEntity(parentUrl, member);
            } else {
                newUrl = dto.toEntity(member);
            }
            return repository.save(newUrl).toDto();
        }catch (Exception e){
            log.error("URL 등록 실패 : {}", e.getMessage());
            throw new RuntimeException("URL 등록을 실패했습니다 : " + dto.getUrl());
        }
    }

    private InspectionUrl retrieveById(Long id) {
        return repository.findById(id).orElseThrow(
                () -> new NoSuchElementException("URL을 찾을 수 없습니다.")
        );
    }

    @Override
    public List<InspectionUrlResponseDto> retrieveAll(Long memberId) {
        List<InspectionUrl> urls = repository.findAllByMemberId(memberId);
        return urls.stream().map(url -> url.toDto()).toList();
    }

    @Override
    public List<InspectionUrlResponseDto> retrieveChildUrl(Long memberId, Long parentUrlId) {
        List<InspectionUrl> childUrls = repository.findAllByMemberIdAndParentId(memberId, parentUrlId);
        return childUrls.stream().map(url -> url.toDto()).toList();
    }

    @Override
    public InspectionUrlResponseDto retrieveUrl(Long urlId, Long memberId) {
        return repository.findByIdAndMemberId(urlId, memberId).orElseThrow(
                () -> new NoSuchElementException("해당 URL을 찾을 수 없습니다.")
        ).toDto();
    }

    private void isExistsInspectionUrl(String url, Long memberId) {
        if (repository.existsByUrlAndMemberId(url, memberId))
            throw new DuplicateFieldException("이미 등록된 URL 입니다.");
    }

}
