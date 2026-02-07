package com.study.spring.member.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.study.spring.member.dto.MemberInfoEmailCheckDTO;
import com.study.spring.member.dto.MemberInfoNicknameCheckDTO;
import com.study.spring.member.repository.MemberInfoRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class MemberInfoCheckController {

    @Autowired
    private MemberInfoRepository memberinforepo;
    
    // 회원가입 auth.user email 중복체크 : 'Y' 중복 , 'N' 중복없음
    @GetMapping("/api/member_InfoEmailChk")
    public Optional<MemberInfoEmailCheckDTO> memberInfoEmailCheckYn(
            @RequestParam(name = "email") String email){
        // ...
        Optional<MemberInfoEmailCheckDTO> memberInfoEmailCheckYn = memberinforepo.memberInfoEmailCheckYn(email);
        return memberInfoEmailCheckYn;
    }
 // 회원가입 auth.user nickname 중복체크 : 'Y' 중복 , 'N' 중복없음
    @GetMapping("/api/member_InfoNicknameChk")
    public Optional<MemberInfoNicknameCheckDTO> memberInfoNicknameCheckYn(
            @RequestParam(name = "nickname") String nickname){
        // ...
        Optional<MemberInfoNicknameCheckDTO> memberInfoNicknameCheckYn = memberinforepo.memberInfoNicknameCheckYn(nickname);
        return memberInfoNicknameCheckYn;
    }
}