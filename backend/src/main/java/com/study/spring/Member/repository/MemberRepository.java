package com.study.spring.Member.repository;

import com.study.spring.Member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface MemberRepository extends JpaRepository<Member, UUID> {
    // 닉네임 중복 확인 로직 (Spring Data JPA가 쿼리 자동 생성)
    boolean existsByNickname(String nickname);
}