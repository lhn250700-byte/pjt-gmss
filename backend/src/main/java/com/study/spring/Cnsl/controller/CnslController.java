package com.study.spring.Cnsl.controller;

import java.lang.String;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import com.study.spring.Cnsl.entity.CounselingStatus;
import com.study.spring.Cnsl.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

import com.study.spring.Cnsl.repository.CnslRepository;
import com.study.spring.Cnsl.service.CnslService;

@RestController
public class CnslController {
	@Autowired
	CnslService cnslService;

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
			@RequestParam("memberId") String memberId,
			@RequestParam("cnslerId") String cnslerId,
			@RequestParam("cnslDt") LocalDate cnslDt,
			@RequestParam("cnslStartTime") LocalTime cnslStartTime) {
		Optional<IsCnslDto> isCounseling =  cnslService.isCounseling(memberId, cnslerId, cnslDt, cnslStartTime);
		return isCounseling;
	}

	// 상담사의 특정 일자 예약 리스트
	@GetMapping("/api/cnslAvailability")
	public List<CnslerDateDto> getAvailableSlots(
			@RequestParam("cnslerId") String cnslerId,
			@RequestParam("cnslDt") LocalDate cnslDt) {

		return cnslService.getAvailableSlotsForCnsler(cnslerId, cnslDt);
	}
	
	@PatchMapping("/api/reserve/{cnslId}")
	public ResponseEntity<?> updateMyCounseling(
            @PathVariable Long cnslId,
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
	public ResponseEntity<?> cancelMyCounseling(@PathVariable Long cnslId) {
		try {
			cnslService.removeMyCounseling(cnslId);
			return ResponseEntity.ok("삭제 성공");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("delete failure: " + e.getMessage());
		}
		
	}

	// [상담사 월별 상담 건수]
	@GetMapping("/api/counselSum/{cnslerId}/monthly")
	public List<CnslDatePerMonthClassDto> getMyCounselingMonthlyCount(@PathVariable String cnslerId) {
		return cnslService.findCounselingMonthlyCountByCounselor(cnslerId);
	}

	// [상담사 전체 건수]
	@GetMapping("/api/counselSum/{cnslerId}")
	public Optional<CnslSumDto> getMyCounselingTotalCount(@PathVariable String cnslerId) {
		return cnslService.findCounselingTotalCountByCounselor(cnslerId);
	}

	// [상담 내역(전체) 조건 없음]
	@GetMapping("/api/cnslList/{cnslerId}")
	public ResponseEntity<Page<cnslListDto>> getMyCounselingList(
			@RequestParam(name="status", required = false) CounselingStatus status,
			@RequestParam(name="page", defaultValue = "0") int page,
			@RequestParam(name="size", defaultValue = "10") int size,
            @PathVariable String cnslerId
	) {
		Pageable pageable = PageRequest.of(page, size);

		Page<cnslListDto> cnslPage = cnslService.findCounselingsByCounselor(status, pageable, cnslerId);
		if (cnslPage.isEmpty()) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.ok(cnslPage);
	}


	// [상담 예약 관리(수락 전)]
	@GetMapping("/api/cnslRsvList/{cnslerId}")
	public ResponseEntity<Page<cnslListWithoutStatusDto>> getPendingReservationList(
			@RequestParam(name="page", defaultValue = "0") int page,
			@RequestParam(name="size", defaultValue = "10") int size,
            @PathVariable String cnslerId
	) {
		Pageable pageable = PageRequest.of(page, size);

		Page<cnslListWithoutStatusDto> rsvPage = cnslService.findPendingReservations(pageable, cnslerId);
		if (rsvPage.isEmpty()) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.ok(rsvPage);
	}
}
