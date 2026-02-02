package com.weba11y.server.api.dto.member;

import com.weba11y.server.domain.member.Member;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class JoinDto {

    @NotBlank
    @Size(min = 4, max = 10, message = "아이디는 4-10자리 영문, 숫자 조합으로 입력해주세요.")
    private String userId;

    @NotBlank
    @Size(min = 8, max = 15, message = "비밀번호는 8-15자리 대소문자 영문, 숫자, 특수기호 조합으로 입력해주세요.")
    private String password;

    @NotBlank
    @Size(min = 2, max = 20, message = "이름은 2-20자리로 입력해주세요.")
    private String name;

    @NotBlank
    @Pattern(regexp = "^\\d{10,15}$", message = "전화번호는 10-15자리 숫자로 입력해주세요.")
    private String phoneNum;

    @NotNull(message = "생일은 필수 입력 사항입니다.")
    private LocalDate birthday;

    public Member toEntity(PasswordEncoder passwordEncoder) {
        return Member.builder()
                .userId(this.userId)
                .password(passwordEncoder.encode(this.password)) // 암호화 된 비밀번호
                .name(this.name)
                .phoneNum(this.phoneNum)
                .birthday(this.birthday)
                .build();
    }
}

