package com.weba11y.server.service.implement;

import com.weba11y.server.checker.AccessibilityChecker;
import com.weba11y.server.domain.InspectionResultReactive;
import com.weba11y.server.dto.InspectionResults.InspectionResultDto;
import com.weba11y.server.dto.InspectionUrl.InspectionUrlDto;
import com.weba11y.server.r2dbc.repository.InspectionResultReactiveRepository;
import com.weba11y.server.service.AccessibilityCheckerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static com.weba11y.server.service.implement.ListUtil.partitionList;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccessibilityCheckerServiceImpl implements AccessibilityCheckerService {

    private final List<AccessibilityChecker> accessibilityCheckers;
    private final InspectionResultReactiveRepository inspectionResultReactiveRepository;
    private final WebClient webClient;
    private static final int BATCH_SIZE = 100;

    @Override
    public Flux<InspectionResultDto> runChecks(InspectionUrlDto inspectionUrl) {
        return fetchHtml(inspectionUrl.getUrl())
                .map(Jsoup::parse)
                .flatMapMany(doc -> Flux.fromIterable(accessibilityCheckers)
                        .flatMap(checker -> checker.check(doc))
                )
                .collectList()
                .flatMapMany(resultDtoList -> {
                    List<InspectionResultReactive> entities = resultDtoList.stream()
                            .map(dto -> dto.toReactiveEntity(inspectionUrl.getId()))
                            .toList();

                    return Flux.fromIterable(partitionList(entities, BATCH_SIZE)) // 배치 단위로 나눔
                            .concatMap(batch -> inspectionResultReactiveRepository.saveAll(batch).then()) // 순차 저장
                            .thenMany(Flux.fromIterable(resultDtoList)); // 저장 후 결과 리턴
                });
    }
    private Mono<String> fetchHtml(String url){
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(e -> log.error("웹 페이지 가져오기 실패: {}", e.getMessage()));

    }
    @Transactional
    public Mono<InspectionResultReactive> save(InspectionResultReactive entity) {
        return inspectionResultReactiveRepository.save(entity);
    }

}
