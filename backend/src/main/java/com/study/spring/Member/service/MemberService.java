package com.study.spring.Member.service;

import com.study.spring.Member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    /**
     * 닉네임 중복 체크
     * @return 중복이면 true, 사용 가능하면 false
     */
    @Transactional(readOnly = true)
    public boolean isNicknameDuplicated(String nickname) {
        return memberRepository.existsByNickname(nickname);
    }
   

    /**
     * email 중복 체크
     * @return 중복이면 true, 사용 가능하면 false
     */
    @Transactional(readOnly = true)
    public boolean isEmailDuplicated(String email) {
        return false; 
    }
}