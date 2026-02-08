package com.study.spring.Cnsl.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.lang.String;

import com.study.spring.Cnsl.entity.CounselingStatus;
import com.study.spring.Cnsl.dto.*;
import com.study.spring.cnslInfo.entity.CnslInfo;
import com.study.spring.cnslInfo.entity.CnslerSchd;
import com.study.spring.cnslInfo.repository.CnslInfoRepository;
import com.study.spring.cnslInfo.repository.CnslerSchdRepository;
import com.study.spring.wallet.entity.PointHistory;
import com.study.spring.wallet.entity.Wallet;
import com.study.spring.wallet.repository.PointHistoryRepository;
import com.study.spring.wallet.repository.WalletRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.study.spring.Cnsl.entity.Cnsl_Reg;
import com.study.spring.Cnsl.repository.CnslRepository;
import com.study.spring.Member.entity.Member;
import com.study.spring.Member.repository.MemberRepository;

import jakarta.transaction.Transactional;

@Service
@Slf4j
public class CnslService {
    @Autowired
    private CnslRepository cnslRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private CnslInfoRepository cnslInfoRepository;
    @Autowired
    private PointHistoryRepository pointHistoryRepository;
    @Autowired
    private CnslerSchdRepository cnslerSchdRepository;

    // [상담 예약]
    @Transactional
    public Integer reserveCounseling(CnslReqDto cnslReqDto) {
        Member member = memberRepository.findByEmail(cnslReqDto.getMember_id())
                .orElseThrow(() -> new IllegalArgumentException("사용자가 없습니다."));
        Member counselor = memberRepository.findByEmail(cnslReqDto.getCnsler_id())
                .orElseThrow(() -> new IllegalArgumentException("상담사가 없습니다."));

        // [상담 시간 밸리데이션]
        if (cnslReqDto.getCnsl_date().isBefore(LocalDate.now()) ||
                (cnslReqDto.getCnsl_date().isEqual(LocalDate.now()) && cnslReqDto.getCnsl_start_time().isBefore(LocalTime.now())))
            throw new IllegalArgumentException("과거 시간은 예약할 수 없습니다.");

        // [사용자 기준 : 상담 진행 중인 경우 재상담 요청 불가] => 유저 기준 이 날에 예약이 있는지 확인 (수정 예정)
        Optional<IsCnslDto> yn = isCounseling(member.getMemberId(), counselor.getMemberId(), cnslReqDto.getCnsl_date());
        yn.ifPresent(res -> {
            if ("Y".equals(res.getIsCounselingYn())) {
                throw new IllegalStateException("사용자님께서 이미 진행 중인 상담이 있습니다.");
            }
        });

        // [해당 일자, 시간에 상담사가 상담 예약이 있는지 확인] => 상담사 기준 이 날에 예약이 있는지 확인 (수정 예정)
        Optional<IsCnslv2Dto> cnslYn = cnslRepository.isCnsl(cnslReqDto.getCnsler_id(), cnslReqDto.getCnsl_date(), cnslReqDto.getCnsl_start_time());
        cnslYn.ifPresent(res ->  {
            if ("Y".equals(res.getIsCnslYn())) {
                throw new IllegalStateException("이미 진행 중인 상담이 있습니다.");
            }
        });

        // [상담사 영업 시간에 대한 에러 처리]
        CnslerSchd cnslerSchd = cnslerSchdRepository.findSchduleByEmail(cnslReqDto.getCnsler_id())
                .orElseThrow(() -> new IllegalArgumentException("상담 영업 정보가 없습니다."));
        String startTime = cnslerSchd.getStartTime();
        String endTime = cnslerSchd.getEndTime();
        if (cnslReqDto.getCnsl_start_time().isBefore(LocalTime.parse(startTime)))
            throw new IllegalStateException("영업 시간보다 전 시간입니다.");
        if (cnslReqDto.getCnsl_start_time().isAfter(LocalTime.parse(endTime)))
            throw new IllegalStateException("영업 시간 이후 시간입니다.");

        // [현재 잔액 get]
        Wallet wallet = walletRepository.findByEmail(cnslReqDto.getMember_id())
                .orElseThrow(() -> new IllegalArgumentException("지갑 정보가 없습니다."));
        Long currPoint = wallet.getCurrPoint();

        // [해당 상담사의 상담 금액 get]
        Long cnslPrice = cnslInfoRepository.findCnslPrice(cnslReqDto.getCnsler_id(), cnslReqDto.getCnsl_tp());
        // [포인트 잔액 부족 시 에러 처리]
        if (currPoint < cnslPrice) throw  new IllegalStateException("보유 포인트가 상담 금액보다 부족합니다.");

        Cnsl_Reg cnslReg = Cnsl_Reg.builder()
                .cnslerId(counselor)
                .memberId(member)
                .cnslCate(cnslReqDto.getCnsl_cate())
                .cnslTp(cnslReqDto.getCnsl_tp())
                .cnslTitle(cnslReqDto.getCnsl_title())
                .cnslContent(cnslReqDto.getCnsl_content())
                .cnslDt(cnslReqDto.getCnsl_date())
                .cnslStartTime(cnslReqDto.getCnsl_start_time())
                .cnslStat("A")
                .cnslTodoYn("Y")
                .delYn("N")
                .build();

        cnslRepository.save(cnslReg);

        // [포인트 내역 생성]
        PointHistory  pointHistory = PointHistory
                .builder()
                .memberId(member)
                .amount(-1 * cnslPrice)
                .pointAfter(currPoint - cnslPrice)
                .cnslId(cnslReg.getCnslId())
                .brief("상담 신청")
                .build();

        pointHistoryRepository.save(pointHistory);

        // [포인트 지갑 입력]
        wallet.setCurrPoint(currPoint - cnslPrice);
        walletRepository.save(wallet);

        return cnslReg.getCnslId();
    }

    // 신청 시 밸리데이션 체크
    public Optional<IsCnslDto> isCounseling(String memberId, String cnslerId, LocalDate cnsl_date) {
        return cnslRepository.isCounseling(memberId, cnslerId, cnsl_date);

    }

    // [상담사 특정 일자 예약 리스트] : 이걸 통해 프론트에서 해당 일자에 상담 가능한 시간을 보여줄 거임
    public List<CnslerDateDto> getAvailableSlotsForCnsler(String cnslerId, LocalDate cnslDt) {
        List<CnslerDateDto> results = cnslRepository.getReservedInfo(cnslerId, cnslDt);

        results.forEach(r -> log.info("상담사ID: {}, 날짜: {}, 시작시간: {}, 회원닉네임: {}",
                r.getCnslDt(),
                r.getCnslStartTime(),
                r.getNickname()
        ));

        return results;
    }

    // 상담 수정 + 삭제에서 getMemberId와 로그인한 email 불일치 시 삭제 및 수정 못하게 로직 처리 해야 함(아직 로그인 기능이 없어서 구현 못함)
    // [상담 수정]
    @Transactional
    public Long modifyMyCounseling(Long cnslId, CnslModiReqDto cnslModiReqDto) {
        // [cnslId 존재 여부 확인]
        Cnsl_Reg cnsl_Reg = cnslRepository.findById(cnslId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상담입니다."));

        // [상담 수정 가능 여부 확인 (status = A 아닐 때 or delYn = Y일 때 터짐]
        if (!"A".equals(cnsl_Reg.getCnslStat()) || "Y".equals(cnsl_Reg.getDelYn()))
            throw new IllegalStateException("수정 불가능한 상담 상태입니다.");

        // [해당 상담사의 영업 시간 중 예약 가능한 시간]
        // 아직 코드 미작성

        // 상담 수정 가능 시간 유효성 체크]
        // 환불 정책이 아직 정해지지 않아서 코드 미작성

        // patch 작동 방식에 의해 null이면 기존 값 유지, null이 아니면 새로운 값 세팅
        if (cnslModiReqDto.getCnsl_date() != null) {
            cnsl_Reg.setCnslDt(cnslModiReqDto.getCnsl_date());
        }

        if (cnslModiReqDto.getCnsl_start_time() != null) {
            cnsl_Reg.setCnslStartTime(cnslModiReqDto.getCnsl_start_time());
        }

        if (cnslModiReqDto.getCnsl_title() != null) {
            cnsl_Reg.setCnslTitle(cnslModiReqDto.getCnsl_title());
        }

        if (cnslModiReqDto.getCnsl_content() != null) {
            cnsl_Reg.setCnslContent(cnslModiReqDto.getCnsl_content());
        }

        return cnslId;
    }

    // [상담 취소]
    public void removeMyCounseling(Long cnslId) {
        Cnsl_Reg cnsl_Reg = cnslRepository.findById(cnslId).orElseThrow(() -> new IllegalArgumentException("예약된 상담이 없습니다."));
        // !cnsl_Reg.getMemberId().equals(현재 로그인한 member_id) throw new AccesException ~
        // [상담 취소 가능 여부]
        if (!"A".equals(cnsl_Reg.getCnslStat())) throw new IllegalStateException("삭제 불가능한 상담 상태입니다.");
        // [취소 되어 있는지 확인]
        if ("Y".equals(cnsl_Reg.getDelYn())) throw new IllegalStateException("이미 취소된 상담입니다.");
        // [취소 가능 시간 제한]
        LocalDateTime counselStartAt = LocalDateTime.of(cnsl_Reg.getCnslDt(), cnsl_Reg.getCnslStartTime());
        LocalDateTime cancelDeadline = counselStartAt.minusDays(1);
        LocalDateTime now = LocalDateTime.now();

        if (now.isAfter(cancelDeadline)) throw new IllegalStateException("상담 시작 1일 전까지만 취소 가능합니다.");

        cnsl_Reg.setDelYn("Y");
        cnsl_Reg.setCnslStat("X");
        cnslRepository.save(cnsl_Reg);
    }

    // [상담사 월별 상담 건수]
    public List<CnslDatePerMonthClassDto> findCounselingMonthlyCountByCounselor(String cnslerId) {
        /*
         * 1. 쿼리로 startMonth와 cnt를 가져온다.
         * 2. 모든 것을 DB에 맡기는 것을 방지하기 위해 스프링에서 endMonth를 구한다.
         * 3. 이때, 쿼리값을 가져오는 DTO는 interface기 때문에 class 형태의 Dto를 추가로 만들어 값을 저장한다.
         * */
        List<CnslDatePerMonthDto> results = cnslRepository.getCnslDatePerMonthList(cnslerId);

        return results.stream().map(r -> {
            LocalDate monthStart = r.getMonthStart();
            LocalDate monthEnd = YearMonth.from(monthStart).atEndOfMonth();

            return CnslDatePerMonthClassDto
                    .builder()
                    .monthStart(monthStart)
                    .monthEnd(monthEnd)
                    .totalCnt(r.getTotalCnt())
                    .reservedCnt(r.getReservedCnt())
                    .completedCnt(r.getCompletedCnt())
                    .build();
        }).toList();
    }

    // [상담사 전체 건수]
    public Optional<CnslSumDto> findCounselingTotalCountByCounselor(String cnslerId) {
        return cnslRepository.getCnslTotalCount(cnslerId);
    }

    // [상담 내역(전체)]
    public Page<cnslListDto> findCounselingsByCounselor(CounselingStatus status, Pageable pageable, String cnslerId) {
        String stat = status == null ? null : status.name();
        return cnslRepository.findCounselingsByCounselor(stat, pageable, cnslerId);
    }

    // [상담 예약 관리(수락 전)]
    public Page<cnslListWithoutStatusDto> findPendingReservations(Pageable pageable, String cnslerId) {
        return cnslRepository.findPendingReservations(pageable, cnslerId);
    }
}
