package com.weba11y.server.dto.member;


import lombok.*;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class LoginResultDto {
    private String message;
    private Long id;
    private String username;
    private String accessToken;
}
