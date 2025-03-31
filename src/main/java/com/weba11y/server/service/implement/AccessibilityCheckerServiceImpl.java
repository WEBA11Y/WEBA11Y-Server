package com.weba11y.server.service.implement;

import com.weba11y.server.checker.AccessibilityChecker;
import com.weba11y.server.checker.implement.*;
import com.weba11y.server.domain.InspectionResultReactive;
import com.weba11y.server.dto.InspectionResults.InspectionResultDto;
import com.weba11y.server.dto.InspectionUrl.InspectionUrlDto;
import com.weba11y.server.r2dbc.repository.InspectionResultReactiveRepository;
import com.weba11y.server.service.AccessibilityCheckerService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccessibilityCheckerServiceImpl implements AccessibilityCheckerService {

    private List<AccessibilityChecker> accessibilityCheckers;
    private final InspectionResultReactiveRepository inspectionResultReactiveRepository;

    @PostConstruct
    public void init() {
        accessibilityCheckers = new ArrayList<>();
        // 모든 접근성 검사기를 리스트에 추가
        accessibilityCheckers.add(new AltTextCheck());
        accessibilityCheckers.add(new AltMultimediaCheck());
        accessibilityCheckers.add(new AutoPlayCheck());
    }

    @Override
    public Flux<InspectionResultDto> runChecks(InspectionUrlDto inspectionUrl) {
        return Flux.defer(() -> {
            try {
                Document doc = Jsoup.connect(inspectionUrl.getUrl()).get(); // URL에서 Document 객체 생성
                // 모든 검사기를 사용하여 검사 수행
                return Flux.fromIterable(accessibilityCheckers)
                        .flatMap(checker -> checker.check(doc)
                                .flatMap(resultDto -> {
                                    // InspectionResultDto를 InspectionResult로 변환 후 DB에 저장
                                    InspectionResultReactive entity = resultDto.toReactiveEntity(inspectionUrl.getId());
                                    return save(entity) // DB에 저장
                                            .then(Mono.just(resultDto)); // 저장 후 원래 DTO 반환
                                })
                        );
            } catch (Exception e) {
                return Flux.error(new RuntimeException("URL을 가져오는 데 실패했습니다."));
            }
        });
    }

    @Transactional
    public Mono<InspectionResultReactive> save(InspectionResultReactive entity) {
        return inspectionResultReactiveRepository.save(entity);
    }

}
