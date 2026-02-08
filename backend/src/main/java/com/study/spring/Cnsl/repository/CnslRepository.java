package com.study.spring.Cnsl.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.lang.String;

import com.study.spring.Cnsl.entity.CounselingStatus;
import com.study.spring.Cnsl.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.study.spring.Cnsl.entity.Cnsl_Reg;

@Repository
public interface CnslRepository extends JpaRepository<Cnsl_Reg, Long> {


    @Query(value = """ 
            	SELECT CASE WHEN COUNT(*) > 0 THEN 'Y' ELSE 'N' END AS isCounselingYn 
            	FROM cnsl_reg cr, member u, member c 
            	WHERE cr.member_id = :memberId 
            	AND cr.cnsler_id = :cnslerId 
            	AND cr.cnsl_stat IN ('A','C')
            	AND cr.cnsl_dt = :cnslDt
            """, nativeQuery = true)
    Optional<IsCnslDto> isCounseling(@Param("memberId") String memberId, @Param("cnslerId") String cnslerId, @Param("cnslDt") LocalDate cnslDt);

    @Query(value = """
            select case when count(*) > 0 then 'Y' else 'N' as isCnslYn
            from cnsl_reg
            where cnsler_id = :cnslerId
            and del_yn = 'N'
            and cnsl_stat in ('B', 'C')
            and cnsl_dt = :cnslDt and cnsl_start_time = :cnslStartTime
            """, nativeQuery = true)
    Optional<IsCnslv2Dto> isCnsl(@Param("cnslerId") String cnslerId, @Param("cnslDt") LocalDate cnslDt, @Param("cnslStartTime") LocalTime cnslStartTime);

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
    List<CnslerDateDto> getReservedInfo(@Param("cnslerId") String cnslerId, @Param("cnslDt") LocalDate cnslDt);

    @Query(value = """
            select
             date_trunc('month', cr.cnsl_dt)::date AS month_start,
             COUNT(*) AS total_cnt,
             sum(case when cr.cnsl_stat = 'A' then 1 else 0 end) reserved_cnt,
             sum(case when cr.cnsl_stat = 'D' then 1 else 0 end) completed_cnt
             from cnsl_reg cr
             where cr.cnsler_id = :cnslerId
             and coalesce(cr.del_yn, 'N') = 'N'
             and cr.cnsl_stat in ('A', 'D')
             group by month_start
             order by month_start desc
            """, nativeQuery = true)
    List<CnslDatePerMonthDto> getCnslDatePerMonthList(@Param("cnslerId") String cnslerId);

    @Query(value = """
            	select
                count(*) as total_cnt,
                sum(case when cr.cnsl_stat = 'A' then 1 else 0 end) reserved_cnt,
                sum(case when cr.cnsl_stat = 'D' then 1 else 0 end) completed_cnt
            	from cnsl_reg cr
            	WHERE cr.cnsler_id = :cnslerId
            	and cr.del_yn = 'N'
            """, nativeQuery = true)
    Optional<CnslSumDto> getCnslTotalCount(@Param("cnslerId") String cnslerId);

    // [상담 내역 전체]
    @Query(value = """
            	select
            	cr.cnsl_id,
            	cr.cnsl_title,
            	cr.cnsl_content,
            	m.nickname,
            	case when cr.cnsl_stat = 'C' and cr.cnsl_todo_yn = 'Y'
            	  then '답변 필요'
            	  else '!'	
            	end as respYn,  
            	case when cr.cnsl_stat = 'D'
            	  then to_char(cr.cnsl_dt, 'YY.MM.DD') || ' ' || to_char(cr.cnsl_end_time, 'HH24:MI')
            	  else to_char(cr.cnsl_dt, 'YY.MM.DD') || ' ' || to_char(cr.cnsl_start_time, 'HH24:MI')
            	end as dt_time,
                case 
                    when cr.cnsl_stat = 'B' then '상담 예정'
                    when cr.cnsl_stat = 'C' then '상담 진행 중'
                    when cr.cnsl_stat = 'D' then '상담 완료'
                    else '!'
                end as statusText
            	from cnsl_reg cr
            	join member m
              on cr.member_id = m.member_id
              where cr.del_yn = 'N'
              and cr.cnsler_Id = :cnslerId
              and (cr.cnsl_stat is null or cr.cnsl_stat = :status
              ) 
            """, nativeQuery = true)
    Page<cnslListDto> findCounselingsByCounselor(@Param("status") String status, Pageable pageable, @Param("cnslerId") String cnslerId);

    // [상담 예약 관리(수락 전)]
    @Query(value = """
            select
                cr.cnsl_id,
                cr.cnsl_title,
                cr.cnsl_content,
                m.nickname, 
                case when cr.cnsl_stat = 'D'
                  then to_char(cr.cnsl_dt, 'YY.MM.DD') || ' ' || to_char(cr.cnsl_end_time, 'HH24:MI')
                  else to_char(cr.cnsl_dt, 'YY.MM.DD') || ' ' || to_char(cr.cnsl_start_time, 'HH24:MI')
                end as dt_time
             from cnsl_reg cr
             join member m
             on cr.member_id = m.member_id
             where cr.del_yn = 'N'
             and cr.cnsl_stat = 'A'
            """, nativeQuery = true)
    Page<cnslListWithoutStatusDto> findPendingReservations(Pageable pageable, @Param("cnslerId") String cnslerId);
}
