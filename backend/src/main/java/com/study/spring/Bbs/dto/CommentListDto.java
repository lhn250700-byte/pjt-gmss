package com.study.spring.Bbs.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentListDto {

	private String bbs_div;
	private String title;
	private String content;
	private String nickname;
	private LocalDateTime created_at;
	private Long cmtCount;
	private Long clikeCount;
}
