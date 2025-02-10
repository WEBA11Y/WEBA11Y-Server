package com.weba11y.server.service.implement;

import com.weba11y.server.domain.InspectionUrl;
import com.weba11y.server.domain.Member;
import com.weba11y.server.dto.InspectionUrl.InspectionUrlRequestDto;
import com.weba11y.server.dto.InspectionUrl.InspectionUrlResponseDto;
import com.weba11y.server.exception.custom.DuplicateFieldException;
import com.weba11y.server.exception.custom.InvalidateTokenException;
import com.weba11y.server.repository.InspectionUrlRepository;
import com.weba11y.server.service.InspectionUrlService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class InspectionUrlServiceImpl implements InspectionUrlService {
    private final InspectionUrlRepository repository;
    private static final String URL_REGEX = "^(https?://)(www\\.)?([a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}(/.*)?$";

    private static final Pattern URL_PATTERN = Pattern.compile(URL_REGEX);
    @Transactional
    @Override
    public InspectionUrlResponseDto saveUrl(InspectionUrlRequestDto dto, Member member) {
        // 이미 등록된 URL 인지 확인
        isExistsInspectionUrl(dto.getUrl(), member.getId());
        try {
            InspectionUrl newUrl;
            // 부모 URL 유무
            if (dto.getParentId() != null && dto.getParentId() > 0) {
                InspectionUrl parentUrl = retrieveUrlById(dto.getParentId());
                newUrl = dto.toEntity(parentUrl, member);
            } else {
                newUrl = dto.toEntity(member);
            }
            return repository.save(newUrl).toDto();
        } catch (Exception e) {
            log.error("URL 등록 실패 : {}", e.getMessage());
            throw new RuntimeException("URL 등록을 실패했습니다 : " + dto.getUrl());
        }
    }

    @Override
    public boolean validateUrl(String url) {
        return URL_PATTERN.matcher(url).matches()
                ? doesUrlExist(url)
                : false;
    }


    private boolean doesUrlExist(String url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("HEAD"); // HEAD 요청을 보내어 응답을 확인
            connection.setConnectTimeout(5000); // 연결 타임아웃 설정
            connection.setReadTimeout(5000); // 읽기 타임아웃 설정
            connection.connect();

            int responseCode = connection.getResponseCode();
            return (responseCode >= 200 && responseCode < 400); // 200~399 응답 코드는 유효한 URL
        } catch (IOException e) {
            return false; // 예외 발생 시 URL이 존재하지 않음
        }
    }


    @Transactional
    @Override
    public InspectionUrlResponseDto updateUrl(InspectionUrlRequestDto requestDto, Long urlId) {
        InspectionUrl url = retrieveUrlById(urlId);
        url.update(requestDto);
        return url.toDto();
    }

    @Override
    public String deleteUrl(Long urlId, Long memberId) {
        InspectionUrl url = retrieveUrlById(urlId);
        if (url.getMember().getId() != memberId)
            throw new InvalidateTokenException("권한이 없습니다");
        try {
            url.delete();
            return "URL을 정상적으로 삭제했습니다. \n삭제된 URL은 30일간 보관됩니다.";
        } catch (Exception e) {
            log.error("URL 삭제 실패 : {}", e.getMessage());
            throw new RuntimeException("URL 삭제를 실패했습니다.");
        }

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

    private InspectionUrl retrieveUrlById(Long urlId) {
        return repository.findByUrlId(urlId).orElseThrow(
                () -> new NoSuchElementException("해당 URL을 찾을 수 없습니다.")
        );
    }

    private void isExistsInspectionUrl(String url, Long memberId) {
        if (repository.existsByUrlAndMemberId(url, memberId))
            throw new DuplicateFieldException("이미 등록된 URL 입니다.");
    }

}
