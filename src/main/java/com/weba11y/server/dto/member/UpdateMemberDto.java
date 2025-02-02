package com.weba11y.server.dto.member;


import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UpdateMemberDto {

    @NotBlank(message = "올바른 전화번호 형식이 아닙니다.")
    private String phoneNum;
}
