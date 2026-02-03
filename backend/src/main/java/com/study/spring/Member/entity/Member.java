package com.study.spring.Member.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
//@Table(name = "\"user\"")
@Table(name = "member", schema = "public")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {
	@Id
	@Column(name = "member_id", columnDefinition ="UUID") 
	private UUID memberId; // (UUID)
	
	private String role; // 1. 상담자, 2. 상담사, 3. 관리자 (Code 테이블 'role' 매핑 )

	// nullable=false → 무조건 값 있어야 함
	@Column(nullable = false, unique = true)
	private String nickname;
	private String gender; // M/F (Code 테이블 'gender' 매핑)
	private String mbti; 
	private LocalDate birth; 
	private String persona;
    // 상담사 전용 정보
    private String profile;
    private String text;
	private LocalDateTime updatedAt;
	private LocalDateTime createdAt;
	
	@PrePersist
	public void onCreate() {
		this.createdAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();
	}
	
	@PreUpdate
	public void onUpdate() {
		this.updatedAt = LocalDateTime.now();
	}
}