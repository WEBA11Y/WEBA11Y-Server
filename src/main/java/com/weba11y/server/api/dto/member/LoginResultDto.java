package com.weba11y.server.api.dto.member;


import lombok.*;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class LoginResultDto {
    private String message;
    private Long id;
    private String userId;
    private String accessToken;
}
