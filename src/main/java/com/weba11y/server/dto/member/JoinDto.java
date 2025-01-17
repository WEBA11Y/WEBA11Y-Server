package com.weba11y.server.dto.member;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class JoinDto {

    @NotBlank
    @Size(min = 4, max = 10, message = "아이디는 4-10자리 영문, 숫자 조합으로 입력해주세요.")
    private String username;

    @NotBlank
    @Size(min = 8, max = 15, message = "비밀번호는 8-15자리 대소문자 영문, 숫자, 특수기호 조합으로 입력해주세요.")
    private String password;

    @NotBlank
    @Size(min = 2, max = 20, message = "이름은 2-20자리로 입력해주세요.")
    private String name;

    @NotBlank
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    @NotBlank
    @Pattern(regexp = "^\\d{10,15}$", message = "전화번호는 10-15자리 숫자로 입력해주세요.")
    private String phoneNum;

    @NotNull(message = "생일은 필수 입력 사항입니다.")
    private LocalDate birthday;
}

