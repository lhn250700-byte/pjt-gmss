package com.study.spring.bbs.entity;

import java.time.LocalDateTime;
import java.util.Map;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "bbs")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Bbs {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // pk, increment 대응
    @Column(name="bbs_id")
    private Integer bbsId;

	@Column(nullable = false)
    private String bbs_div; // (Code 테이블 'bbs_div' 매핑)
	
	private String mbti;
	private String title;
	private String content;
	private Integer views;
	@Column(name="img_name")
	private String imgName;
	@Column(name="img_url")
	private String imgUrl;	
	@Column(name="del_yn")
	private String delYn;
	
	@CreationTimestamp
    @Column
    private LocalDateTime created_at;

	@UpdateTimestamp
    private LocalDateTime updated_at;
	
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "hash_tags", columnDefinition = "jsonb")
	private Map<String, Object> hashTags;

	// Vector 처리:pgvector-java 라이브러리 등을 사용하거나 float[]로 매핑합니다.
	@Column(columnDefinition = "vector(1536)")
	private float[] embedding;
}
