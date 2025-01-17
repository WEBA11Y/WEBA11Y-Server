package com.weba11y.server.dto.member;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class LoginDto {
    @NotBlank
    @Size(min = 4, max = 10, message = "아이디는 4-10자리 영문, 숫자 조합으로 입력해주세요.")
    private String username;

    @NotBlank
    @Size(min = 8, max = 15, message = "비밀번호는 8-15자리 대소문자 영문, 숫자, 특수기호 조합으로 입력해주세요.")
    private String password;
}
