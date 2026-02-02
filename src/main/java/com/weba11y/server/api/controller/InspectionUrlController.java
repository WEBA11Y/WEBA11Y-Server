package com.weba11y.server.api.controller;

import com.weba11y.server.infrastructure.security.CurrentMemberId;
import com.weba11y.server.api.dto.InspectionDetailDto;
import com.weba11y.server.api.dto.inspectionUrl.InspectionUrlDto;
import com.weba11y.server.application.service.InspectionUrlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "URL 관리 API", description = "URL을 관리하는 API입니다.")
public class InspectionUrlController {

    private final InspectionUrlService inspectionUrlService;

    // URL 등록
    @PostMapping("/api/v1/urls")
    @Operation(summary = "URL 등록", description = "URL을 등록합니다. ( 상위 URL이 있다면 해당 URL의 ID값을 추가하세요. )")
    public ResponseEntity<InspectionUrlDto.Response> registerUrl(@RequestBody @Valid InspectionUrlDto.Request requestDto,
                                                                 @CurrentMemberId Long memberId) {
        return ResponseEntity.ok().body(inspectionUrlService.saveUrl(requestDto, memberId).toResponse());
    }

    // 모든 URL 조회
    @GetMapping("/api/v1/urls")
    @Operation(summary = "등록된 모든 상위 URL 조회", description = "회원이 등록한 모든 상위 URL을 조회합니다.")
    public ResponseEntity<InspectionUrlDto.ParentOnlyResponse> getAllUrl(@RequestParam(defaultValue = "1") int page,
                                                                         @CurrentMemberId Long memberId) {
        return ResponseEntity.ok().body(inspectionUrlService.retrieveParentUrl(memberId, page));
    }

    // URL 조회
    @GetMapping("/api/v1/urls/{id}")
    @Operation(summary = "선택한 URL 정보 조회", description = "선택한 URL의 정보를 조회합니다.")
    public ResponseEntity<InspectionDetailDto> getUrl(@PathVariable("id") Long urlId, @CurrentMemberId Long memberId) {
        return ResponseEntity.ok().body(inspectionUrlService.getInspectionUrlDetail(urlId, memberId));
    }

    // URL
    @PutMapping("/api/v1/urls/{id}")
    @Operation(summary = "등록된 URL 정보 수정", description = "URL의 정보를 수정합니다.")
    public ResponseEntity<InspectionUrlDto.Response> updateUrl(@PathVariable("id") Long urlId,
                                                               @RequestBody @Valid InspectionUrlDto.Request requestDto) {
        return ResponseEntity.ok().body(inspectionUrlService.updateUrl(requestDto, urlId).toResponse());
    }

    // URL 삭제
    @DeleteMapping("/api/v1/urls")
    @Operation(summary = "등록된 URL 삭제", description = "URL 정보를 삭제합니다.")
    public ResponseEntity<Void> deleteUrl(@RequestBody List<Long> urlIds, @CurrentMemberId Long memberId) {
        return ResponseEntity.status(inspectionUrlService.deleteUrl(urlIds, memberId)).build();
    }

    // 하위 URL 조회
    @GetMapping("/api/v1/urls/{id}/children")
    @Operation(summary = "하위 URL 조회", description = "상위 URL과 직접적으로 관계를 가지는 하위 URL 리스트를 제공.")
    public ResponseEntity<?> getDirectChildrenUrl(@PathVariable("id") Long urlId, @CurrentMemberId Long memberId) {
        return ResponseEntity.ok().body(null);
    }

    // 전체 하위 URL 조회`
    @GetMapping("/api/v1/urls/{id}/descendants")
    @Operation(summary = "모든 하위 URL 조회", description = "상위 URL을 기준으로 관련된 모든 하위 URL(하위의 하위 등) 리스트를 제공.")
    public ResponseEntity<?> getAllDescendantsUrl(@PathVariable("id") Long urlId, @CurrentMemberId Long memberId) {
        return ResponseEntity.ok().body(null);
    }
}
