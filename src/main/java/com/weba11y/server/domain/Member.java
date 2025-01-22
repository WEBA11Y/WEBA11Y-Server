package com.weba11y.server.domain;

import com.weba11y.server.domain.common.BaseEntity;
import com.weba11y.server.domain.enums.MemberStatus;
import com.weba11y.server.domain.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String username;

    @Column(nullable = false, length = 250)
    private String password;

    @Column(nullable = false, length = 20)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

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
    public Member(String username, String password, String name, String email, String phoneNum, LocalDate birthday) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.email = email;
        this.phoneNum = phoneNum;
        this.birthday = birthday;
        this.role = Role.ROLE_USER; // 기본 역할
        this.status = MemberStatus.ACTIVE; // 기본 상태
    }

    // 업데이트 메서드
    public void update(String email, String phoneNum) {
        this.email = email;
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
}
