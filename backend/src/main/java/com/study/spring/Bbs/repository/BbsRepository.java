package com.study.spring.Bbs.repository;

import com.study.spring.Bbs.dto.CommentListDto;
import com.study.spring.Bbs.dto.PopularPostDto;
import com.study.spring.Bbs.dto.PostListDto;
import com.study.spring.Bbs.entity.Bbs;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BbsRepository extends JpaRepository<Bbs, Integer> {
	@Query(value = """
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
	List<PopularPostDto> findPopularPosts();

	@Query(value = """
			select
			b.bbs_id,
			b.bbs_div,
			b.title,
			b.views,
			b.created_at,
			m.nickname,
			COUNT(bl.like_id) as like_count
			from bbs b
			left join member m on m.member_id = b.member_id
			left join bbs_like bl on bl.bbs_id = b.bbs_id
			where b.member_id = :memberId
			group by
			b.bbs_id,
			b.bbs_div,
			b.title,
			b.views,
			b.created_at,
			m.nickname
			order by b.created_at DESC
			""", countQuery = """
			select count(*)
			from bbs b
			where b.member_id = :memberId
			""", nativeQuery = true)
	Page<PostListDto> getPostListByMemberId(@Param("memberId") String memberId, Pageable pageable);

	@Query(value = """
			select
			b.bbs_div,
			b.title,
			bc.content,
			m.nickname,
			bc.created_at,
			count(bc.cmt_id) as cmt_count,
			count(cl.clike_id) as clike_count
			from bbs_comment bc
			left join bbs b on b.bbs_id = bc.bbs_id
			left join member m on m.member_id = bc.member_id
			left join cmt_like cl on cl.cmt_id = bc.cmt_id
			where bc.member_id = :memberId
			group by
			b.bbs_div,
			b.title,
			bc.content,
			m.nickname,
			bc.created_at
			order by bc.created_at DESC
			""", countQuery = """
			select count(*)
			from bbs b
			where b.member_id = :memberId
			""",nativeQuery = true)
	Page<CommentListDto> getCommentListByMemberId(@Param("memberId") String memberId, Pageable pageable);
}
