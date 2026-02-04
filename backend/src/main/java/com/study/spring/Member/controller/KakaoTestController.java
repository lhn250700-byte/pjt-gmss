package com.study.spring.Member.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

@Controller
public class KakaoTestController {
    @GetMapping("/kakao/auth-code") // 카카오에서 설정한 주소(redirect-uri)
    public ResponseEntity<String> kakaoCallback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String error,
            @RequestParam String state
    ) {
        // 1. 인가 코드 로그
        System.out.println("인가 코드: " + code);
        System.out.println("이메일: " + state  );

        // 2. 인가 코드 → 토큰
        KakaoTokenResponse token = getAccessToken(code);
        System.out.println("access_token: " + token.accessToken());

        // 3. 토큰 → 사용자 정보
        KakaoUserInfoResponse userInfo =
                getUserInfo(token.accessToken());

        System.out.println("kakaoId: " + userInfo.id());
        System.out.println("nickname: " +
                userInfo.kakaoAccount().profile().nickname());

        return ResponseEntity.ok("카카오 로그인 성공");
    }

    private KakaoTokenResponse getAccessToken(String code) {

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", "db84a36a76acabde3b54a4323ecec380");
        body.add("redirect_uri", "http://localhost:8080/kakao/auth-code");
        body.add("code", code);
        body.add("client_secret", "NcLc5uPCtDjEA8dixgMsm3UntN6dywp2");

        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<KakaoTokenResponse> response =
                restTemplate.postForEntity(
                        "https://kauth.kakao.com/oauth/token",
                        request,
                        KakaoTokenResponse.class
                );

        return response.getBody();
    }

    private KakaoUserInfoResponse getUserInfo(String accessToken) {

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<KakaoUserInfoResponse> response =
                restTemplate.exchange(
                        "https://kapi.kakao.com/v2/user/me",
                        HttpMethod.GET,
                        request,
                        KakaoUserInfoResponse.class
                );

        return response.getBody();
    }

    public record KakaoTokenResponse(

            @JsonProperty("access_token")
            String accessToken,

            @JsonProperty("token_type")
            String tokenType,

            @JsonProperty("refresh_token")
            String refreshToken,

            @JsonProperty("expires_in")
            Long expiresIn,

            @JsonProperty("scope")
            String scope
    ) {}

    public record KakaoUserInfoResponse(

            @JsonProperty("id")
            Long id,

            @JsonProperty("connected_at")
            String connectedAt,

            @JsonProperty("kakao_account")
            KakaoAccount kakaoAccount
    ) {

        public record KakaoAccount(

                @JsonProperty("profile")
                Profile profile
        ) {
            public record Profile(

                    @JsonProperty("nickname")
                    String nickname,

                    @JsonProperty("profile_image_url")
                    String profileImageUrl
            ) {}
        }
    }

}
