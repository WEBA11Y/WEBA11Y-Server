package com.weba11y.server.controller;


import com.weba11y.server.domain.Member;
import com.weba11y.server.dto.InspectionUrl.InspectionUrlRequestDto;
import com.weba11y.server.dto.InspectionUrl.InspectionUrlResponseDto;
import com.weba11y.server.service.AuthService;
import com.weba11y.server.service.InspectionUrlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "URL 관리 API", description = "URL을 관리하는 API입니다.")
public class InspectionUrlController {

    private final InspectionUrlService inspectionUrlService;
    private final AuthService authService;

    // URL 등록
    @PostMapping("/api/v1/urls")
    @Operation(summary = "URL 등록", description = "URL을 등록합니다. ( 상위 URL이 있다면 해당 URL의 ID값을 추가하세요. )")
    public ResponseEntity<?> registerUrl(@RequestBody @Valid InspectionUrlRequestDto requestDto, Principal principal) {
        Member member = authService.retrieveMember(getMemberId(principal));
        return ResponseEntity.ok().body(inspectionUrlService.saveUrl(requestDto, member));
    }

    // 모든 URL 조회
    @GetMapping("/api/v1/urls")
    @Operation(summary = "등록된 모든 URL 조회", description = "회원이 등록한 모든 URL을 조회합니다.")
    public ResponseEntity<List<InspectionUrlResponseDto>> getAllUrl(Principal principal) {
        return ResponseEntity.ok().body(inspectionUrlService.retrieveAll(getMemberId(principal)));
    }

    // URL 조회
    @GetMapping("/api/v1/urls/{id}")
    @Operation(summary = "선택한 URL 정보 조회", description = "선택한 URL의 정보를 조회합니다.")
    public ResponseEntity<?> getUrl(@PathVariable("id") Long urlId, Principal principal) {

        return ResponseEntity.ok().body(inspectionUrlService.retrieveUrl(urlId, getMemberId(principal)));
    }

    // URL 수정
    @PutMapping("/api/v1/urls/{id}")
    @Operation(summary = "URL 정보 수정", description = "URL의 정보를 수정합니다.")
    public ResponseEntity<?> updateUrl(@PathVariable("id") Long urlId,
                                       @RequestBody @Valid InspectionUrlRequestDto requestDto) {
        return ResponseEntity.ok().body(inspectionUrlService.updateUrl(requestDto, urlId));
    }

    // URL 삭제
    @DeleteMapping("/api/v1/urls/{id}")
    public ResponseEntity<?> deleteUrl(@PathVariable("id") Long urlId) {
        return ResponseEntity.ok().body("");
    }

    private Long getMemberId(Principal principal) {
        return principal instanceof UsernamePasswordAuthenticationToken
                ? (Long) ((UsernamePasswordAuthenticationToken) principal).getPrincipal()
                : -1L;
    }
}
