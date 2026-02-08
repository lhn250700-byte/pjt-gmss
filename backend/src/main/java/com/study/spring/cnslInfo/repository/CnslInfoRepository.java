package com.study.spring.cnslInfo.repository;

import com.study.spring.cnslInfo.entity.CnslInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CnslInfoRepository extends JpaRepository<CnslInfo, Long> {
    @Query(value = """
        select cnsl_price 
        from cnsl_info 
        where  member_id = :email and cnsl_tp = :cnslTp
    """, nativeQuery = true)
    Long findCnslPrice(@Param("email") String email, @Param("cnslTp") String cnslTp);
}
