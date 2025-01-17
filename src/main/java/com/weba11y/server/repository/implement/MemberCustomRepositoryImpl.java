package com.weba11y.server.repository.implement;

import com.weba11y.server.domain.Member;
import com.weba11y.server.dto.member.JoinDto;
import com.weba11y.server.repository.MemberCustomRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MemberCustomRepositoryImpl implements MemberCustomRepository {
    private final EntityManager em;

    @Override
    public Optional<Member> findByUsername(String username) {
        try {
            Member member = em.createQuery("select m from Member m where m.username =: username", Member.class)
                    .setParameter("username", username)
                    .getSingleResult();
            return Optional.of(member);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean existsUniqueInfo(JoinDto joinDto) {
        Long count =  em.createQuery("select count(m) from Member m " +
                                "where m.username =:username or m.email =: email or m.phoneNum =: phoneNum"
                        , Long.class)
                .setParameter("username", joinDto.getUsername())
                .setParameter("email", joinDto.getEmail())
                .setParameter("phoneNum", joinDto.getPhoneNum())
                .getSingleResult();
        return count == 0;
    }
}
