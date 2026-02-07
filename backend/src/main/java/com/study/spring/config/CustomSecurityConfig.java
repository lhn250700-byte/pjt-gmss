package com.study.spring.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.study.spring.security.handler.APILoginFailHandler;
import com.study.spring.security.handler.APILoginSuccessHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Configuration
@Log4j2
@EnableMethodSecurity
public class CustomSecurityConfig {
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		log.info("---------------------security config---------------------------");
		
		http.csrf(config -> config.disable());
		http.cors(config -> config.disable());
		http.sessionManagement(sessionConfig ->  sessionConfig.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
		http.formLogin(config -> {
		      config.loginPage("/api/member/login");
		      config.successHandler(new APILoginSuccessHandler());
		      config.failureHandler(new APILoginFailHandler());
//		      config.successHandler(new APILoginSuccessHandler());
//		      config.failureHandler(new APILoginFailHandler());
		    });
		
		return http.build();
	}
}
