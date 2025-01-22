package com.weba11y.server.dto.member;

import com.weba11y.server.domain.Member;
import com.weba11y.server.domain.enums.MemberStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberDto {
    private Long id;
    private String username;
    private String name;
    private String email;
    private String phoneNum;
    private LocalDate birthday;
    private MemberStatus status;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;

    public static MemberDto of(Member member) {
        return MemberDto.builder()
                .id(member.getId())
                .username(member.getUsername())
                .name(member.getName())
                .email(member.getEmail())
                .phoneNum(member.getPhoneNum())
                .birthday(member.getBirthday())
                .status(member.getStatus())
                .createDate(member.getCreateDate())
                .updateDate(member.getUpdateDate())
                .build();
    }
}

