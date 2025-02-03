package com.weba11y.server.repository;

import com.weba11y.server.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;


public interface MemberRepository extends JpaRepository<Member, Long>, MemberCustomRepository {
    boolean existsByUserId(String username);

    boolean existsByPhoneNum(String phoneNum);
}
