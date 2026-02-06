package com.study.spring.member.repository;

import com.study.spring.member.dto.MemberInfoEmailCheckDTO;
import com.study.spring.member.dto.MemberInfoNicknameCheckDTO;
import com.study.spring.member.entity.Member;

import org.springframework.data.jpa.repository.JpaRepository; // JpaRepository 사용 가능
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface MemberInfoRepository extends JpaRepository<Member, String> {

    // DTO 인터페이스를 반환 타입으로 지정
    @Query(value = """
            SELECT CASE WHEN COUNT(*) > 0 THEN 'Y' ELSE 'N' END AS userInfoEmailCheckYn
            FROM member
            WHERE email = :email
            """,
            nativeQuery = true)
    Optional<MemberInfoEmailCheckDTO> memberInfoEmailCheckYn(@Param("email") String email);
    
    // DTO 인터페이스를 반환 타입으로 지정
    @Query(value = """
            SELECT CASE WHEN COUNT(*) > 0 THEN 'Y' ELSE 'N' END AS userInfoNicknameCheckYn
            FROM member
            WHERE nickname = :nickname
            """,
            nativeQuery = true)
    Optional<MemberInfoNicknameCheckDTO> memberInfoNicknameCheckYn(@Param("nickname") String nickname);
}
