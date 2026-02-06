package com.study.spring.Bbs.controller;

import com.study.spring.Bbs.dto.PopularPostClassDto;
import com.study.spring.Bbs.dto.PopularPostDto;
import com.study.spring.Bbs.service.BbsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class BbsController {
    @Autowired
    BbsService bbsService;

    // [실시간 인기글]
    @GetMapping("/posts/popular/realtime")
    public List<PopularPostClassDto> getRealtimePopularPosts (){
        return bbsService.findRealtimePopularPosts();
    }

    // [주간 인기글]
    @GetMapping("/posts/popular/weekly")
    public List<PopularPostClassDto> getWeeklyPopularPosts (){
        return bbsService.findWeeklyPopularPosts();
    }

    // [월간 인기글]
    @GetMapping("/posts/popular/monthly")
    public void getMonthlyPopularPosts (){

    }

    // [추천순]
    @GetMapping("/posts/recommendation")
    public void getRecommendedPosts (){

    }

}
