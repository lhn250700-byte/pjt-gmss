package com.study.spring.Cnsl.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.study.spring.Cnsl.dto.CnslDatePerMonthDto;
import com.study.spring.Cnsl.dto.CnslSumDto;
import com.study.spring.Cnsl.dto.CnslerDateDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.study.spring.Cnsl.dto.IsCnslDto;
import com.study.spring.Cnsl.entity.Cnsl_Reg;

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

	@Query(value= """
		select
			date_trunc('month', cr.cnsl_dt)::date AS month_start,
			COUNT(*) AS total_cnt,
			COUNT(*) FILTER (WHERE cr.cnsl_stat = 'A') AS reserved_cnt,
			COUNT(*) FILTER (WHERE cr.cnsl_stat = 'D') AS completed_cnt
		from cnsl_reg cr
		where cr.cnsler_id = :cnslerId
		and cr.del_yn = 'N'
		and cr.cnsl_stat in ('A', 'D')
		group by month_start
		order by month_start desc
	""", nativeQuery = true)
	List<CnslDatePerMonthDto> getCnslDatePerMonthList(@Param("cnslerId") UUID cnslerId);

	@Query(value= """
		select
			count(*) as total_cnt,
			count(*) filter (where cr.cnsl_stat = 'A') as reserved_cnt,
			count(*) filter (where cr.cnsl_stat = 'D') as completed_cnt
		from cnsl_reg cr
		WHERE cr.cnsler_id = :cnslerId
		and cr.del_yn = 'N'
	""", nativeQuery = true)
	Optional<CnslSumDto> getCnslTotalCount(@Param("cnslerId") UUID cnslerId);

	Page<?> findCounselingsByCounselor(Pageable pageable, UUID cnslerId);

	Page<?> findPendingReservations(Pageable pageable, UUID cnslerId);
}
