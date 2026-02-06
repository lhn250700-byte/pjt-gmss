package com.study.spring.security.handler;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.google.gson.Gson;
import com.study.spring.member.dto.MemberDto;
import com.study.spring.util.JWTUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class APILoginSuccessHandler implements AuthenticationSuccessHandler{

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {

		log.info("------------------------Login success--------------------------------");
		// 인증사용자의 정보값을 가지고 온다. memberDto의 정보값
		log.info(authentication.getPrincipal());
		log.info("------------------------Login success--------------------------------");
		
		MemberDto memberDto = (MemberDto) authentication.getPrincipal();
		
		log.info(memberDto);
		
		Map<String, Object> claims = memberDto.getClaims();
		
		// access , refresh token
		// claims에 넣어서 access token : 10분동안 사용 : 
		// refresh token : 60 * 24 : HTTPonly cookie값
		claims.put("accessToken", JWTUtil.generateToken(claims, 10));
		claims.put("refreshToken", JWTUtil.generateToken(claims, 60*24));
		
		Gson gson = new Gson();
		String jsonStr = gson.toJson(claims);
		
		response.setContentType("application/json:charset=UTF-8");
		PrintWriter printWriter = response.getWriter();
		printWriter.println(jsonStr);
		printWriter.close();
	
		
		// content type UTF+8
		
	}

}
