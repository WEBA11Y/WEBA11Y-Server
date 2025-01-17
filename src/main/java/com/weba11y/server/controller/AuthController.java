package com.weba11y.server.controller;


import com.weba11y.server.dto.member.JoinDto;
import com.weba11y.server.dto.member.LoginDto;
import com.weba11y.server.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


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
}
