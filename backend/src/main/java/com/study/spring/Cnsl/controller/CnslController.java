package com.study.spring.Cnsl.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.study.spring.Cnsl.dto.CnslerDateDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.study.spring.Cnsl.dto.CnslModiReqDto;
import com.study.spring.Cnsl.dto.CnslReqDto;
import com.study.spring.Cnsl.dto.IsCnslDto;
import com.study.spring.Cnsl.repository.CnslRepository;
import com.study.spring.Cnsl.service.CnslService;

@RestController
public class CnslController {
	@Autowired
	CnslService cnslService;
	@Autowired
	CnslRepository cnslRepository;

	@PostMapping("/api/reserve")
	public ResponseEntity<?> createCounselingReservation(@RequestBody CnslReqDto cnslReqDto) {
		try {
			Integer id = cnslService.reserveCounseling(cnslReqDto);
			return ResponseEntity.ok(id);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Apply failure: " + e.getMessage());	
		}
	}
	
	// 예약 벨리데이션 체크
	@GetMapping("/api/iscnslyn") 
	public Optional<IsCnslDto> isCounseling(
			@RequestParam("memberId") UUID memberId, 
			@RequestParam("cnslerId") UUID cnslerId,
			@RequestParam("cnslDt") LocalDate cnslDt) {
		Optional<IsCnslDto> isCounseling =  cnslService.isCounseling(memberId, cnslerId, cnslDt);
		return isCounseling;
	}

	// 상담사의 특정 일자 예약 리스트
	@GetMapping("/api/cnslAvailability")
	public List<CnslerDateDto> getAvailableSlots(
			@RequestParam("cnslerId") UUID cnslerId,
			@RequestParam("cnslDt") LocalDate cnslDt) {

		return cnslService.getAvailableSlotsForCnsler(cnslerId, cnslDt);
	}
	
	@PatchMapping("/api/reserve/{cnslId}")
	public ResponseEntity<?> updateMyCounseling(
		@PathVariable("cnslId") Long cnslId,
        @RequestBody CnslModiReqDto cnslModiReqDto
	) {
		try {
			Long id = cnslService.modifyMyCounseling(cnslId, cnslModiReqDto);
			return ResponseEntity.ok(id);
			
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("update failure: " + e.getMessage());
		}
	}
	
	@PutMapping("/api/cancel/{cnslId}")
	public ResponseEntity<?> cancelMyCounseling(@PathVariable("cnslId") Long cnslId) {
		try {
			cnslService.removeMyCounseling(cnslId);
			return ResponseEntity.ok("삭제 성공");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("delete failure: " + e.getMessage());
		}
		
	}
}
