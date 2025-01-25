package com.weba11y.server.controller;


import com.weba11y.server.dto.member.JoinDto;
import com.weba11y.server.dto.member.LoginDto;
import com.weba11y.server.dto.member.MemberDto;
import com.weba11y.server.dto.member.UpdateMemberDto;
import com.weba11y.server.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;


@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/api/v1/login")
    public ResponseEntity<?> memberLogin(@RequestBody @Valid LoginDto loginDto,
                                         HttpServletResponse res) {
        return ResponseEntity.ok().body(authService.login(loginDto, res));
    }

    @PostMapping("/api/v1/join")
    public ResponseEntity<?> memberJoin(@RequestBody @Valid JoinDto joinDto) {
        return ResponseEntity.ok().body(authService.join(joinDto));
    }

    @GetMapping("/api/v1/member")
    public ResponseEntity<MemberDto> getMember(Principal principal) {
        Long memberId = getMemberId(principal);
        return ResponseEntity.ok().body(authService.retrieveMember(memberId).toDto());
    }

    @PutMapping("/api/v1/member")
    public ResponseEntity<?> updateMember(@RequestBody @Valid UpdateMemberDto memberDto,
                                          Principal principal) {
        Long memberId = getMemberId(principal);
        return ResponseEntity.ok().body(authService.updateMember(memberId, memberDto));
    }

    @DeleteMapping("/api/v1/member")
    public ResponseEntity<?> deleteMember(Principal principal){
        Long memberId = getMemberId(principal);
        return ResponseEntity.ok().body(authService.deleteMember(memberId));
    }


    @GetMapping("/api/v1/join/check-username")
    public ResponseEntity<Boolean> checkUsernameExists(@RequestBody @Valid String username) {
        return ResponseEntity.ok().body(authService.isExistsUsername(username));
    }

    @GetMapping("/api/v1/join/check-email")
    public ResponseEntity<Boolean> checkEmailExists(@RequestBody @Valid String email) {
        return ResponseEntity.ok().body(authService.isExistsEmail(email));
    }

    @GetMapping("/api/v1/join/check-phoneNum")
    public ResponseEntity<Boolean> checkPhoneNumExists(@RequestBody @Valid String phoneNum) {
        return ResponseEntity.ok().body(authService.isExistsPhoneNum(phoneNum));
    }

    private Long getMemberId(Principal principal) {
        return principal instanceof UsernamePasswordAuthenticationToken
                ? (Long) ((UsernamePasswordAuthenticationToken) principal).getPrincipal()
                : -1L;
    }
}
