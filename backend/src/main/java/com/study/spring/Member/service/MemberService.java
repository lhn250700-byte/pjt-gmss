package com.study.spring.Member.service;

import com.study.spring.Member.dto.MemberDto;
import com.study.spring.Member.dto.MemberInfoEmailCheckDTO;
import com.study.spring.Member.dto.SignUpDto;
import com.study.spring.Member.entity.Member;
import com.study.spring.Member.entity.MemberRole;
import com.study.spring.Member.repository.MemberInfoRepository;
import com.study.spring.Member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberInfoRepository memberInfoRepository;
    private final PasswordEncoder passwordEncoder;

    public void register(SignUpDto request) {
        Optional<MemberInfoEmailCheckDTO> checkResults = memberInfoRepository.memberInfoEmailCheckYn(request.getEmail());

        // [소셜 + 계정 존재]
        checkResults.ifPresent(res -> {
            if ("Y".equals(res.getUserInfoEmailCheckYn()) && res.getSocial()) {
                throw new IllegalStateException(String.format(
                        "소셜 로그인 충돌: 이메일 '%s'은 이미 기존 계정에 연결되어 있습니다.",
                        request.getEmail()
                ));
            } // [계정 존재]
            else if ("Y".equals(res.getUserInfoEmailCheckYn()) && !res.getSocial()) {
                throw new IllegalStateException(String.format(
                        "일반 회원 가입 충돌: 이메일 '%s'은 이미 사용 중입니다.",
                        request.getEmail()
                ));
            }
        });

        // [비밀번호 더블 체크 실패]
        if (!request.getPassword().equals(request.getPasswordConfirm())) throw new IllegalArgumentException("비밀번호가 일치하지 않습니다. 다시 확인해 주세요.");

        String encode = passwordEncoder.encode(request.getPassword());

        Member member = Member
                .builder()
                .memberId(request.getEmail())
                .pw(encode)
                .nickname(request.getNickname())
                .social(request.isSocial())
                .gender(request.getGender())
                .mbti(request.getMbti())
                .birth(request.getBirth())
                .persona(request.getPersona())
                .profile(request.getProfile())
                .text(request.getText())
                .build();

        member.addRole(MemberRole.USER);
        memberRepository.save(member);
    }

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