package com.study.spring.Bbs.service;

import com.study.spring.Bbs.dto.PopularPostClassDto;
import com.study.spring.Bbs.dto.PopularPostDto;
import com.study.spring.Bbs.repository.BbsRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@Log4j2
public class BbsService {
    @Autowired
    BbsRepository bbsRepository;

    // [실시간 인기글]
    public List<PopularPostClassDto> findRealtimePopularPosts() {
        List<PopularPostDto> results = bbsRepository.findPopularPosts();
        return results.stream().map(r -> PopularPostClassDto
                .builder()
                .bbsId(r.getBbsId())
                .title(r.getTitle())
                .content(r.getContent())
                .views(r.getViews())
                .commentCount(r.getCommentCount())
                .bbsLikeCount(r.getBbsLikeCount())
                .bbsDislikeCount(r.getBbsDisLikeCount())
                .cmtLikeCount(r.getCmtLikeCount())
                .cmtDislikeCount(r.getCmtDisLikeCount())
                .createdAt(r.getCreatedAt())
                .postScore(calculateRealtimeScore(r))
                .build())
                .sorted(Comparator.comparing(PopularPostClassDto::getPostScore).reversed()
                        .thenComparing(PopularPostClassDto::getCreatedAt, Comparator.reverseOrder()))
                .limit(10)
                .toList();
    }

    public Double calculateRealtimeScore(PopularPostDto popularPostDto) {
        //score = (조회수 * 1) + (댓글 수 * 3) + (게시글 좋아요 수 * 5) + (댓글 좋아요 수 * 1.5) - (게시글 싫어요 * 6) - (댓글 싫어요 * 2) / (경과시간 + 1)^α(1.0, 1.2 ~ 1.5)
        Duration duration = Duration.between(popularPostDto.getCreatedAt(), LocalDateTime.now());
        Double time = duration.getSeconds() / 3600.0;
        Double timeScore = Math.pow(time + 1, 1.2);
        Double score = (popularPostDto.getViews() + (popularPostDto.getCommentCount() * 3) + (popularPostDto.getBbsLikeCount() * 5) + (popularPostDto.getCmtLikeCount() * 1.5) - (popularPostDto.getBbsDisLikeCount() * 6) - (popularPostDto.getCmtDisLikeCount()* 2)) / timeScore;

        return score;
    }

    // [주간 인기글]
    public List<PopularPostClassDto> findWeeklyPopularPosts() {
        List<PopularPostDto> results = bbsRepository.findPopularPosts();
        return results.stream().map(r -> PopularPostClassDto
                .builder()
                .bbsId(r.getBbsId())
                .title(r.getTitle())
                .content(r.getContent())
                .views(r.getViews())
                .commentCount(r.getCommentCount())
                .bbsLikeCount(r.getBbsLikeCount())
                .bbsDislikeCount(r.getBbsDisLikeCount())
                .cmtLikeCount(r.getCmtLikeCount())
                .cmtDislikeCount(r.getCmtDisLikeCount())
                .createdAt(r.getCreatedAt())
                .postScore(calculateWeeklyScore(r))
                .build())
                .sorted(Comparator.comparing(PopularPostClassDto::getPostScore).reversed()
                        .thenComparing(PopularPostClassDto::getCreatedAt, Comparator.reverseOrder()))
                .limit(10)
                .toList();
    }

    public Double calculateWeeklyScore(PopularPostDto popularPostDto) {
        // weekly_score = (주간 조회수 * 1) + (주간 댓글 수 * 2) + (주간 게시글 좋아요 수 * 3) + (주간 댓글 좋아요 수 * 1) - (주간 게시글 싫어요 수 * 4) - (주간 댓글 싫어요 * 1.5)
        Double score = popularPostDto.getViews() + (popularPostDto.getCommentCount() * 2) + (popularPostDto.getBbsLikeCount() * 3) + popularPostDto.getCmtLikeCount() - (popularPostDto.getBbsDisLikeCount() * 4) - (popularPostDto.getCmtDisLikeCount() * 1.5);

        return score;
    }

    // [월간 인기글]
    public void findMonthlyPopularPosts() {

    }

    // [추천순]
    public void findRecommendedPosts() {

    }

}
