package com.weba11y.server.domain.member;

import com.weba11y.server.domain.common.BaseEntity;
import com.weba11y.server.domain.enums.MemberStatus;
import com.weba11y.server.domain.enums.Role;
import com.weba11y.server.api.dto.member.MemberDto;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String userId;

    @Column(nullable = false, length = 250)
    private String password;

    @Column(nullable = false, length = 20)
    private String name;

    @Column(nullable = false, unique = true, length = 15)
    private String phoneNum;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberStatus status;

    @Column
    private LocalDate birthday;

    @Builder
    public Member(String userId, String password, String name, String phoneNum, LocalDate birthday) {
        this.userId = userId;
        this.password = password;
        this.name = name;
        this.phoneNum = phoneNum;
        this.birthday = birthday;
        this.role = Role.ROLE_USER; // 기본 역할
        this.status = MemberStatus.ACTIVE; // 기본 상태
    }

    // 업데이트 메서드
    public void update( String phoneNum) {
        this.phoneNum = phoneNum;
    }

    // 회원 비활성화
    public void deactivate() {
        this.status = MemberStatus.DEACTIVATED;
    }

    // 회원 비활성화 및 탈퇴
    public void delete() {
        this.status = MemberStatus.DEACTIVATED;
        onDelete();
    }

    public MemberDto toDto() {
        return MemberDto.of(this);
    }
}
