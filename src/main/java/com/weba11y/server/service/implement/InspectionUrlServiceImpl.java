package com.weba11y.server.service.implement;

import com.weba11y.server.domain.InspectionUrl;
import com.weba11y.server.domain.Member;
import com.weba11y.server.dto.InspectionUrl.InspectionUrlRequestDto;
import com.weba11y.server.dto.InspectionUrl.InspectionUrlResponseDto;
import com.weba11y.server.repository.InspectionUrlRepository;
import com.weba11y.server.service.InspectionUrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class InspectionUrlServiceImpl implements InspectionUrlService {
    private final InspectionUrlRepository repository;

    @Override
    public InspectionUrlResponseDto saveUrl(InspectionUrlRequestDto dto, Member member) {
        InspectionUrl newUrl;
        // 부모 URL 유무
        if (dto.getParentId() != null && dto.getParentId() > 0) {
            InspectionUrl parentUrl = retrieveById(dto.getParentId());
            newUrl = dto.toEntity(parentUrl, member);
        } else {
            newUrl = dto.toEntity(member);
        }
        return repository.save(newUrl).toDto();
    }

    private InspectionUrl retrieveById(Long id) {
        return repository.findById(id).orElseThrow(
                () -> new NoSuchElementException("URL을 찾을 수 없습니다.")
        );
    }
}
