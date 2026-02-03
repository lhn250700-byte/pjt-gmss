package com.study.spring.Cnsl.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.study.spring.Cnsl.dto.CnslerDateDto;
import com.study.spring.Member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.study.spring.Cnsl.dto.IsCnslDto;
import com.study.spring.Cnsl.entity.Cnsl_Reg;
import com.study.spring.Member.dto.MemberInfoEmailCheckDTO;

@Repository
public interface CnslRepository extends JpaRepository<Cnsl_Reg, Long>{

	
	@Query(value=""" 
			SELECT CASE WHEN COUNT(*) > 0 THEN 'Y' ELSE 'N' END AS isCounselingYn 
			FROM cnsl_reg cr, member u, member c 
			WHERE cr.member_id = :memberId 
			AND cr.cnsler_id = :cnslerId 
			AND cr.cnsl_stat IN ('A','C')
			AND cr.cnsl_dt = :cnsl_date
			
		""", nativeQuery = true) 
	Optional<IsCnslDto> isCounseling(@Param("memberId") UUID memberId, @Param("cnslerId") UUID cnslerId, @Param("cnslDt") LocalDate cnsl_date);


	@Query(value = """
    SELECT
           cr.cnsl_dt AS cnslDt, 
           cr.cnsl_start_time AS cnslStartTime, 
           m.nickname AS nickname
    FROM cnsl_reg cr
    JOIN member m ON cr.member_id = m.member_id
    WHERE cr.del_yn = 'N'
      AND cr.cnsl_stat != 'D'
      AND cr.cnsler_id = :cnslerId
      AND cr.cnsl_dt = :cnslDt
""", nativeQuery = true)
	List<CnslerDateDto> getReservedInfo(@Param("cnslerId") UUID cnslerId, @Param("cnslDt") LocalDate cnslDt);
}
