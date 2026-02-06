package com.study.spring.Bbs.repository;

import com.study.spring.Bbs.dto.PopularPostDto;
import com.study.spring.Bbs.entity.Bbs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BbsRepository extends JpaRepository<Bbs, UUID> {
    @Query(value= """
        select
        b.bbs_id, b.title, b.content, b.views, b.created_at,
        COALESCE(count(distinct c.cmt_id), 0) as commentCount,
        (select COALESCE(COUNT(*), 0) from bbs_like bl WHERE bl.bbs_id = b.bbs_id and is_like = true) bbsLikeCount,
        (select COALESCE(COUNT(*), 0) from bbs_like bl WHERE bl.bbs_id = b.bbs_id and is_like = false) bbsDisLikeCount,
        COALESCE(SUM(cl_sum.cmt_like_cnt), 0) AS cmtLikeCount,
        COALESCE(SUM(cl_sum.cmt_dislike_cnt), 0) cmtDisLikeCount
        from bbs b
        left join bbs_comment c on b.bbs_id = c.bbs_id and COALESCE(b.del_yn, 'N') = 'N'
        left join (select cmt_id,
                          COALESCE(SUM(case when is_like = true then 1 else 0 end), 0) cmt_like_cnt,
                          COALESCE(SUM(case when is_like = false then 1 else 0 end), 0) cmt_dislike_cnt
                     from cmt_like
                     group by cmt_id) cl_sum on c.cmt_id = cl_sum.cmt_id
        and COALESCE(c.del_yn, 'N') = 'N'
        group by b.bbs_id, b.title, b.content, b.views, b.created_at
        order by b.bbs_id
    """, nativeQuery = true)
    List<PopularPostDto> findRealtimePopularPosts();

}
