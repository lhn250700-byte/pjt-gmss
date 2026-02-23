package com.study.spring.Bbs.controller;

import com.study.spring.Bbs.dto.CommentListDto;
import com.study.spring.Bbs.dto.PopularPostClassDto;
import com.study.spring.Bbs.dto.PostListDto;
import com.study.spring.Bbs.repository.BbsRepository;
import com.study.spring.Bbs.service.BbsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class BbsController {

	@Autowired
	BbsService bbsService;

	@Autowired
	BbsRepository bbsRepository;

	// [실시간 인기글]
	@GetMapping("/posts/popular/realtime")
	public List<PopularPostClassDto> getRealtimePopularPosts() {
		return bbsService.findRealtimePopularPosts();
	}

	// [주간 인기글]
	@GetMapping("/posts/popular/weekly")
	public List<PopularPostClassDto> getWeeklyPopularPosts() {
		return bbsService.findWeeklyPopularPosts();
	}

	// [월간 인기글]
	@GetMapping("/posts/popular/monthly")
	public void getMonthlyPopularPosts() {

	}

	// [추천순]
	@GetMapping("/posts/recommendation")
	public void getRecommendedPosts() {

	}

	// 내 작성 글
	@GetMapping("/mypage/postlist")
	public ResponseEntity<Page<PostListDto>> getMyPostList(
			@RequestParam("memberId") String memberId,
			@PageableDefault(size = 20, sort = "created_at", direction= Sort.Direction.DESC)
			Pageable pageable) {
		
		return ResponseEntity.ok(
				bbsRepository.getPostListByMemberId(memberId, pageable)
			);
	}

	// 내 작성 댓글
	@GetMapping("/mypage/commentlist")
	public ResponseEntity<Page<CommentListDto>> getMyCommentList(
			@RequestParam("memberId") String memberId,
			@PageableDefault(size = 20, sort = "created_at", direction= Sort.Direction.DESC)
			Pageable pageable) {
		
		return ResponseEntity.ok(
				bbsRepository.getCommentListByMemberId(memberId, pageable)
			);
	}

}
