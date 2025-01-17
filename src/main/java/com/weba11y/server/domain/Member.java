package com.weba11y.server.domain;

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
public class Member {

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

    @Column(nullable = false, updatable = false)
    private LocalDateTime createDate;

    @Column(nullable = false)
    private LocalDateTime updateDate;

    @Column // 삭제 날짜는 null 가능
    private LocalDateTime deleteDate;

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
        this.createDate = LocalDateTime.now();
        this.updateDate = LocalDateTime.now();
        this.deleteDate = null;
    }

    // 업데이트 메서드
    public void update(String name, String email, String phoneNum, LocalDate birthday) {
        this.name = name;
        this.email = email;
        this.phoneNum = phoneNum;
        this.birthday = birthday;
        this.updateDate = LocalDateTime.now();
    }

    // 회원 비활성화
    public void deactivate() {
        this.status = MemberStatus.DEACTIVATED;
        this.updateDate = LocalDateTime.now();
    }

    // 회원 비활성화 및 탈퇴
    public void delete() {
        this.status = MemberStatus.DEACTIVATED;
        this.updateDate = LocalDateTime.now();
        this.deleteDate = LocalDateTime.now();
    }
}
