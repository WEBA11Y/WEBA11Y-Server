package com.weba11y.server.controller;


import com.weba11y.server.domain.Member;
import com.weba11y.server.dto.InspectionUrl.InspectionUrlRequestDto;
import com.weba11y.server.dto.InspectionUrl.InspectionUrlResponseDto;
import com.weba11y.server.service.AuthService;
import com.weba11y.server.service.InspectionUrlService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class InspectionUrlController {

    private final InspectionUrlService inspectionUrlService;
    private final AuthService authService;

    // URL 등록
    @PostMapping("/api/v1/urls")
    public ResponseEntity<?> registerUrl(@RequestBody @Valid InspectionUrlRequestDto requestDto, Principal principal) {
        Member member = authService.retrieveMember(getMemberId(principal));
        return ResponseEntity.ok().body(inspectionUrlService.saveUrl(requestDto, member));
    }

    // 모든 URL 조회
    @GetMapping("/api/v1/urls")
    public ResponseEntity<List<InspectionUrlResponseDto>> getAllUrl(Principal principal) {
        return ResponseEntity.ok().body(inspectionUrlService.retrieveAll(getMemberId(principal)));
    }

    // URL 조회
    @GetMapping("/api/v1/urls/{id}")
    public ResponseEntity<?> getUrl(@PathVariable("id") Long id) {
        return ResponseEntity.ok().body("");
    }

    // URL 수정
    @PutMapping("/api/v1/urls/{id}")
    public ResponseEntity<?> updateUrl(@PathVariable("id") Long id) {
        return ResponseEntity.ok().body("");
    }

    // URL 삭제
    @DeleteMapping("/api/v1/urls/{id}")
    public ResponseEntity<?> deleteUrl(@PathVariable("id") Long id) {
        return ResponseEntity.ok().body("");
    }

    private Long getMemberId(Principal principal) {
        return principal instanceof UsernamePasswordAuthenticationToken
                ? (Long) ((UsernamePasswordAuthenticationToken) principal).getPrincipal()
                : -1L;
    }
}
