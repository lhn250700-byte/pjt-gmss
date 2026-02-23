package com.study.spring.Cnsl.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MyCnslListDto {
	private String cnsl_title;
	private String nickname;
	private String cnsl_stat;
	private LocalDateTime created_at;
}
