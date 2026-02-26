package com.study.spring.Cnsl.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.lang.String;

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
            select case when count(*) > 0 then 'Y' else 'N' end isCounselingYn
            from cnsl_reg cr
            where cr.del_yn = 'N'
            and cr.cnsl_stat in ('A', 'B', 'C')
            and cr.cnsl_dt = :cnslDt
            and cr.cnsl_start_time = :cnslStartTime
            and (
                  cr.cnsler_id = :cnslerId
               or cr.member_id = :memberId
            )
            """, nativeQuery = true)
    Optional<IsCnslDto> isCounseling(@Param("memberId") String memberId, @Param("cnslerId") String cnslerId, @Param("cnslDt") LocalDate cnslDt, @Param("cnslStartTime") LocalTime cnslStartTime);

    @Query(value = """
                SELECT
               cr.cnsl_dt AS cnslDt,
               cr.cnsl_start_time AS cnslStartTime, 
               m.nickname AS nickname
                FROM cnsl_reg cr
                JOIN member m ON cr.cnsler_id = m.member_id
                WHERE cr.del_yn = 'N'
                  AND cr.cnsl_stat <> 'X'      
                  AND cr.cnsler_id = :cnslerId
                  AND cr.cnsl_dt = :cnslDt
                ORDER BY cr.cnsl_start_time
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

    // [상담 내역 전체 : stat으로 구분됨]
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
              and (cr.cnsl_stat is null or cr.cnsl_stat = :status)
              order by cr.cnsl_dt, cr.cnsl_start_time
            """, nativeQuery = true)
    Page<cnslListDto> findCounselingsByCounselor(@Param("status") String status, Pageable pageable, @Param("cnslerId") String cnslerId);
    
    // [전체 상담 내역]
    @Query(value="""
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
              and cr.cnsl_stat NOT IN('A', 'X')
              ORDER BY
		      CASE 
		        WHEN cr.cnsl_stat = 'B' THEN 1
		        WHEN cr.cnsl_stat = 'C' AND cr.cnsl_todo_yn = 'Y' THEN 2
		        WHEN cr.cnsl_stat = 'C' AND cr.cnsl_todo_yn != 'Y' THEN 3
		        WHEN cr.cnsl_stat = 'D' THEN 4
		        ELSE 5
		      END,
		      cr.cnsl_dt,       
		      cr.cnsl_start_time
    		""", nativeQuery = true)
    Page<cnslListDto> findAllCounselingsByCounselor(Pageable pageable, @Param("cnslerId") String cnslerId);

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
             order by cr.cnsl_dt, cr.cnsl_start_time
            """, nativeQuery = true)
    Page<cnslListWithoutStatusDto> findPendingReservations(Pageable pageable, @Param("cnslerId") String cnslerId);


    @Query(value = """
        select 
            r.cnsler_id, 
            m.nickname,
           sum(case when r.cnsl_stat in ('A','B','C') then 1 else 0 end) as cnslReqCnt,
           sum(case when r.cnsl_stat in ('D') then 1 else 0 end) as cnslDoneCnt,
           count(*)
        from cnsl_reg r
        join member m on r.cnsler_id = m.member_id
        where cnsl_stat not in ('X')
        and cnsl_dt between :startDate and :endDate
        and r.cnsler_id = :cnslerId
        group by cnsler_id, m.nickname
    """, nativeQuery = true)
    ConsultationStatusCountDto findConsultationStatusCounts(@Param("cnslerId") String cnslerId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query(value =
        """
        select 
              c.code,
              c.code_name,
              to_char(coalesce(b.cnsl_price_sum, 0),'999,999,999,999') cnslPriceSum,
              to_char(coalesce(b.cnsl_price_cmsn, 0),'999,999,999,999') cnslPriceCmsn,
              to_char(coalesce(b.cnsl_price_sum, 0)
                    - coalesce(b.cnsl_price_cmsn, 0),'999,999,999,999') cnslExctAmt,
              coalesce(b.cnsl_count, 0) cnslCount
       from code c
       left join (
                select 
                   cr.cnsler_id,
                   m.nickname,
                   cr.cnsl_tp,
                   get_code_nm('cnsl_tp',cr.cnsl_tp) cnsl_tp_nm,
                   sum(coalesce(ci.cnsl_price,0)) cnsl_price_sum,
                   trunc(sum(coalesce(ci.cnsl_price,0) * coalesce(ci.cnsl_rate,0))::numeric,-1) cnsl_price_cmsn , -- 10자리에서 버림
                   count(*) cnsl_count
                from cnsl_reg cr
                join member m on m.member_id = cr.cnsler_id
                left join cnsl_info ci on ci.member_id = cr.cnsler_id and ci.cnsl_tp = cr.cnsl_tp
                where cr.cnsl_stat not in ('X') -- 상담취소제외
                and cr.cnsler_id = :cnslerId
                and cr.cnsl_dt between :startDate and :endDate
                group by cr.cnsler_id, m.nickname, cr.cnsl_tp ) b
                on c.code = b.cnsl_tp
       where c.col_id = 'cnsl_tp'
       order by c.code
    """, nativeQuery = true)
    List<ConsultationCategoryCountDto> findConsultationCategoryCounts(@Param("cnslerId") String cnslerId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query(value = """
        select 
           r.cnsl_dt,
           r.cnsler_id, 
           m.nickname,
           sum(case when r.cnsl_stat in ('A','B','C') then 1 else 0 end) cnslReqCnt,
           sum(case when r.cnsl_stat in ('D') then 1 else 0 end) cnslDoneCnt,
           count(*)
        from cnsl_reg r
        join member m on r.cnsler_id = m.member_id
        where cnsl_stat not in ('X')
        and cnsl_dt between :startDate and :endDate
        and r.cnsler_id = :cnslerId
        group by r.cnsl_dt, cnsler_id, m.nickname
        order by r.cnsl_dt desc
    """, nativeQuery = true)
    List<ConsultationStatusDailyDto> findDailyReservationCompletionTrend(@Param("cnslerId") String cnslerId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query(value = """
            select cr.cnsler_id,
            	   m.nickname,
            	   '기간별수익합' tp,
                   to_char(sum(coalesce(ci.cnsl_price,0)), '999,999,999,999') cnslPriceSum,
                   to_char(trunc(sum(coalesce(ci.cnsl_price,0) * coalesce(ci.cnsl_rate,0))::numeric,-1), '999,999,999,999') cnslPriceCmsn , -- 10자리에서 버림
                   to_char(sum(coalesce(ci.cnsl_price,0))
            			- trunc(sum(coalesce(ci.cnsl_price,0) * coalesce(ci.cnsl_rate,0))::numeric,-1), '999,999,999,999') cnslExctAmt,
                   count(*) cnslCount
            from cnsl_reg cr
            join member m on m.member_id = cr.cnsler_id
            left join cnsl_info ci on ci.member_id = cr.cnsler_id and ci.cnsl_tp = cr.cnsl_tp
            where cr.cnsl_stat not in ('X') -- 상담취소제외
            and cr.cnsler_id = :cnslerId
            and cr.cnsl_dt between :startDate and :endDate
            group by cr.cnsler_id, m.nickname
            union all
            select cr.cnsler_id,
            	   m.nickname,
            	   '3개월별수익합' tp,
                   to_char(sum(coalesce(ci.cnsl_price,0)), '999,999,999,999') cnslPriceSum,
                   to_char(trunc(sum(coalesce(ci.cnsl_price,0) * coalesce(ci.cnsl_rate,0))::numeric,-1), '999,999,999,999') cnslPriceCmsn , -- 10자리에서 버림
                   to_char(sum(coalesce(ci.cnsl_price,0))
            			- trunc(sum(coalesce(ci.cnsl_price,0) * coalesce(ci.cnsl_rate,0))::numeric,-1), '999,999,999,999') cnslExctAmt,
                   count(*) cnslCount
            from cnsl_reg cr
            join member m on m.member_id = cr.cnsler_id
            left join cnsl_info ci on ci.member_id = cr.cnsler_id and ci.cnsl_tp = cr.cnsl_tp
            where cr.cnsl_stat not in ('X') -- 상담취소제외
            and cr.cnsler_id = :cnslerId
            and cr.cnsl_dt between current_date - INTERVAL '3 MONTH' and current_date --current_date
            group by cr.cnsler_id, m.nickname
    """, nativeQuery = true)
    List<MyRevenueSummaryDto> findMyRevenueSummary(@Param("cnslerId") String cnslerId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query(value = """
        select 
               cr.cnsler_id,
               m.nickname,
               cr.cnsl_tp,
               get_code_nm('cnsl_tp',cr.cnsl_tp) cnslTpNm,
               count(*) cnslCount
        from cnsl_reg cr
        join member m on m.member_id = cr.cnsler_id
        left join cnsl_info ci on ci.member_id = cr.cnsler_id and ci.cnsl_tp = cr.cnsl_tp
        where cr.cnsl_stat not in ('X') -- 상담취소제외
        and cr.cnsler_id = :cnslerId
        and cr.cnsl_dt between :startDate and :endDate
        group by cr.cnsler_id, m.nickname, cr.cnsl_tp
        order by count(*) desc, cr.cnsl_tp
        limit 1
    """, nativeQuery = true)
    MostConsultedTypeDto findMostConsultedType(@Param("cnslerId") String cnslerId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query(value=
            """
        select to_char(r.created_at, 'YYYY-MM-DD HH24:MI:SS') risk_date,
               r.table_id,
               r.bbs_div,
               get_code_nm('bbs_div', bbs_div) bbs_div_nm,
               r.member_id,
               m.nickname,
               r.content,
               r.action
        from bbs_risk r
        join  member m on r.member_id = m.member_id
        order by r.created_at desc
    """, nativeQuery = true)
    List<RealtimeRiskDetectionStatusDto> findRealtimeRiskDetectionStatus();

    @Query(value=
            """
        select cr.cnsl_dt,
               cr.cnsler_id,
               m.nickname,
               to_char(sum(coalesce(ci.cnsl_price,0)),'999,999,999,999') cnsl_price_sum,
               to_char(trunc(sum(coalesce(ci.cnsl_price,0) * coalesce(ci.cnsl_rate,0))::numeric,-1),'999,999,999,999') cnsl_price_cmsn , -- 10자리에서 버림
               to_char(sum(coalesce(ci.cnsl_price,0))
               	- trunc(sum(coalesce(ci.cnsl_price,0) * coalesce(ci.cnsl_rate,0))::numeric,-1),'999,999,999,999') cnsl_exct_sum,
               '완료' exct_stat,
               count(*) cnsl_count
        from cnsl_reg cr
        join member m on m.member_id = cr.cnsler_id
        left join cnsl_info ci on ci.member_id = cr.cnsler_id and ci.cnsl_tp = cr.cnsl_tp
        where cr.cnsl_stat not in ('X') -- 상담취소제외
        and cr.cnsl_dt between current_date - 7 and current_date
        group by cr.cnsl_dt, cr.cnsler_id, m.nickname
        order by cr.cnsl_dt desc , sum(coalesce(ci.cnsl_price,0)) desc, cr.cnsler_id
    """, nativeQuery = true)
    Page<CounselorRevenueLatestlyDto> findLatestlyCounselorRevenue(Pageable pageable);

    
    @Query(value= """
            select c.code,
                   c.code_name,
                   to_char(coalesce(b.cnsl_price_sum, 0),'999,999,999,999') cnsl_price_sum,
                   to_char(coalesce(b.cnsl_price_cmsn, 0),'999,999,999,999') cnsl_price_cmsn,
                   to_char(coalesce(b.cnsl_exct_sum,0),'999,999,999,999') cnsl_exct_sum,
                   coalesce(b.cnsl_count,0) cnsl_count,
                   coalesce(avg_cnsl_time, '00:00:00') avg_cnsl_time
            from code c
            left join ( select cr.cnsl_cate,
            			       sum(coalesce(ci.cnsl_price,0)) cnsl_price_sum,
            			       trunc(sum(coalesce(ci.cnsl_price,0) * coalesce(ci.cnsl_rate,0))::numeric,-1) cnsl_price_cmsn , -- 10자리에서 버림
            			       sum(coalesce(ci.cnsl_price,0))
            			       	- trunc(sum(coalesce(ci.cnsl_price,0) * coalesce(ci.cnsl_rate,0))::numeric,-1) cnsl_exct_sum,
            			       to_char(avg(cnsl_end_time - cnsl_start_time), 'HH24:MI:SS') avg_cnsl_time,
            			       count(*) cnsl_count
            			from cnsl_reg cr
            			join member m on m.member_id = cr.cnsler_id
            			left join cnsl_info ci on ci.member_id = cr.cnsler_id and ci.cnsl_tp = cr.cnsl_tp
            			where cr.cnsl_stat not in ('X') -- 상담취소제외
            			and cr.cnsl_dt between :startDate and :endDate
            			group by cr.cnsl_cate ) b on c.code = b.cnsl_cate
            where c.col_id = 'cnsl_cate'
            order by c.code
    """, nativeQuery = true)
    List<CategoryRevenueStatisticsDto> findCategoryRevenueStatistics(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query(value= """
            select c.code,
                   c.code_name,
                   to_char(coalesce(b.cnsl_price_sum, 0),'999,999,999,999') cnsl_price_sum,
                   to_char(coalesce(b.cnsl_price_cmsn, 0),'999,999,999,999') cnsl_price_cmsn,
                   to_char(coalesce(b.cnsl_exct_sum,0),'999,999,999,999') cnsl_exct_sum,
                   coalesce(b.cnsl_count,0) cnsl_count,
                   coalesce(avg_cnsl_time, '00:00:00') avg_cnsl_time
            from code c
            left join ( select cr.cnsl_tp,
            			       sum(coalesce(ci.cnsl_price,0)) cnsl_price_sum,
            			       trunc(sum(coalesce(ci.cnsl_price,0) * coalesce(ci.cnsl_rate,0))::numeric,-1) cnsl_price_cmsn , -- 10자리에서 버림
            			       sum(coalesce(ci.cnsl_price,0))
            			       	- trunc(sum(coalesce(ci.cnsl_price,0) * coalesce(ci.cnsl_rate,0))::numeric,-1) cnsl_exct_sum,
            			       to_char(avg(cnsl_end_time - cnsl_start_time), 'HH24:MI:SS') avg_cnsl_time,
            			       count(*) cnsl_count
            			from cnsl_reg cr
            			join member m on m.member_id = cr.cnsler_id
            			left join cnsl_info ci on ci.member_id = cr.cnsler_id and ci.cnsl_tp = cr.cnsl_tp
            			where cr.cnsl_stat not in ('X') -- 상담취소제외
            			and cr.cnsl_dt between :startDate and :endDate
            			group by cr.cnsl_tp ) b on c.code = b.cnsl_tp
            where c.col_id = 'cnsl_tp'
            order by c.code
    """, nativeQuery = true)
    List<CategoryRevenueStatisticsDto> findTypeRevenueStatistics(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

	
}
