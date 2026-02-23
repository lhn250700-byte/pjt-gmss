import axios from 'axios';
import { BASE_URL } from './config';

/**
 * 상담 관리 API
 * TODO: 백엔드 API 구현 후 실제 엔드포인트로 교체
 */

// 상담 상태 상수
export const COUNSEL_STATUS = {
  PENDING: 'pending', // 대기 중 (수락/거절 전)
  ACCEPTED: 'accepted', // 수락됨 (예정)
  COMPLETED: 'completed', // 완료됨
  CANCELLED: 'cancelled', // 취소됨
  REJECTED: 'rejected', // 거절됨
};

// API Base URL (환경 변수로 관리 권장)
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api';

/**
 * 통계 데이터 가져오기
 */
export const fetchCounselStats = async () => {
  try {
    // TODO: 실제 API 호출로 교체
    // const response = await fetch(`${API_BASE_URL}/counsel/stats`);
    // if (!response.ok) throw new Error('통계 데이터 로드 실패');
    // return await response.json();

    // ========== 더미 데이터 시작 (DB 연동 시 아래 전체 삭제) ==========
    return {
      riskCount: 2, // 위험군 상담 건수
      completedCount: 5, // 완료 상담 건수
      reservedCount: 10, // 예약 상담 건수 (ACCEPTED + PENDING)
      totalCount: 15, // 전체 상담 건수
    };
    // ========== 더미 데이터 끝 (여기까지 삭제) ==========
  } catch (error) {
    console.error('fetchCounselStats error:', error);
    throw error;
  }
};

/**
 * 타임라인 데이터 가져오기
 */
export const fetchCounselTimeline = async () => {
  try {
    // TODO: 실제 API 호출로 교체
    // const response = await fetch(`${API_BASE_URL}/counsel/timeline`);
    // if (!response.ok) throw new Error('타임라인 데이터 로드 실패');
    // return await response.json();

    // ========== 더미 데이터 시작 (DB 연동 시 아래 전체 삭제) ==========
    return [
      { day: '13일', reserved: 2, completed: 1 },
      { day: '15일', reserved: 4, completed: 2 },
      { day: '17일', reserved: 3, completed: 2 },
      { day: '19일', reserved: 3, completed: 1 },
      { day: '21일', reserved: 2, completed: 3 },
      { day: '23일', reserved: 1, completed: 2 },
      { day: '25일', reserved: 2, completed: 1 },
      { day: '27일', reserved: 1, completed: 0 },
      { day: '29일', reserved: 5, completed: 2 },
      { day: '31일', reserved: 3, completed: 2 },
    ];
    // ========== 더미 데이터 끝 (여기까지 삭제) ==========
  } catch (error) {
    console.error('fetchCounselTimeline error:', error);
    throw error;
  }
};

/**
 * 모든 상담 내역 가져오기
 */
export const fetchAllCounsels = async ({ page, size, cnslerId }) => {
  try {
    const { data } = await axios.get(`${BASE_URL}/api/cnslRsvList/${cnslerId}`, {
      params: {
        page,
        size,
      },
    });

    return data;
  } catch (error) {
    console.error('fetchAllCounsels error:', error);
    throw error;
  }
};

/*
 * 상담 상태에 따른 리스트 가져오기 (상담 수락 = B, 상담 진행 중 = C, 상담 끝 = D)
 */
export const fetchCounselsByStatus = async ({ page, size, status, cnslerId }) => {
  try {
    const { data } = await axios.get(`${BASE_URL}/api/cnslList/${cnslerId}`, {
      params: {
        page,
        size,
        status,
      },
    });

    return data;
  } catch (error) {
    console.error('fetchCounsels error:', error);
    throw error;
  }
};

export const fetchCounselsBeforeAccept = async ({ page, size, cnslerId }) => {
  try {
    const { data } = await axios.get(`${BASE_URL}/api/cnslRsvList/${cnslerId}`, {
      params: {
        page,
        size,
      },
    });

    return data;
  } catch (error) {
    console.error('fetchCounselsBeforeAccept error:', error);
    throw error;
  }
};

/**
 * 상담 상세 정보 가져오기
 */
export const fetchCounselDetail = async (counselId) => {
  try {
    // TODO: 실제 API 호출로 교체
    // const response = await fetch(`${API_BASE_URL}/counsel/${counselId}`);
    // if (!response.ok) throw new Error('상담 상세 정보 로드 실패');
    // return await response.json();

    // ========== 더미 데이터 시작 (DB 연동 시 아래 전체 삭제) ==========
    return {
      id: counselId,
      title: '제목 : 너무많은일이있었어힘들다...',
      client: '임살미',
      clientId: 'user-123', // 상담 받는 사람의 ID
      reservationDate: '2026-01-14 16:00',
      status: 'scheduled', // 'scheduled' | 'inProgress' | 'completed'
      chatRoomId: `chat-${counselId}`, // 채팅방 ID
      requestContent: '해야 할 일은 과감히 하며, 결심한 일은 반드시 실행하라. - 벤자민 프랭클린-',
      detailedContent:
        '우리 인생에서 해야 할 일이 참 많지요. 또 새해에 계획하고 결심한 일들도 참 많지요. 그렇지만 여건 상 못하거나 힘들어서 주춤하는 일들이많기도 합니다. 그것을 새해에 계획한 하고 정한 하지 않는다면 이후 소용도 없겠지요. 그래서 새해가 되니 힘들어하는 저에게 저 명언이 참 마음에 와 닿습니다. 오늘도 저 명언을 되새기며 새해가 계획한 일들을 실행하려고 노력해 봅니다.',
      counselor: {
        name: '가을치',
        mbti: 'INTP',
        additionalInfo: '성별 : 남성 / 나이 : 32세',
        image: '/counselor-profile.jpg',
        persona:
          '[web발신] 너는나를초혼해야한다나는발동도로9가야수많은케이드프로틀틀이올렸으므2016유온에서프트루켐이팔고수음응참차점과도시세에서직대터묵점이다도리에게솔이치면다야포드덕초로와시게운시기우아이인도점아원들의성장이다판36세이나이에드프리미어고에서18율리츄대와업셀스로다어야호종지전이인수얼수의룩와컷츰종리술적멧와니은메곤로는다우눌드겐판률술어니기쁨차멧컷이야멧출지지원워지지일과거셈대년도에이먼유눈메에이대년사상앤라서지컨튀열셰스이이라버유함이시이라익무핌에사이일',
      },
    };
    // ========== 더미 데이터 끝 (여기까지 삭제) ==========
  } catch (error) {
    console.error('fetchCounselDetail error:', error);
    throw error;
  }
};

/**
 * 상담 수락하기
 */
export const acceptCounsel = async (cnslId, message) => {
  try {
    // TODO: 실제 API 호출로 교체 => 완료
    const response = await axios.post(`${BASE_URL}/api/approve/${cnslId}`, {
      message,
    });
    if (!response.ok) throw new Error('상담 수락 실패');
    console.log('상담 수락:', cnslId);
    return await response.json();
  } catch (error) {
    console.error('acceptCounsel error:', error);
    throw error;
  }
};

/**
 * 상담 거절하기
 */
export const rejectCounsel = async (cnslId, reason) => {
  try {
    // TODO: 실제 API 호출로 교체 => 완료
    const response = await axios.post(`${BASE_URL}/api/reject/${cnslId}`, {
      message: reason,
    });

    if (!response.ok) throw new Error('상담 거절 실패');
    console.log('상담 거절:', cnslId, reason);
    return await response.json();
  } catch (error) {
    console.error('rejectCounsel error:', error);
    throw error;
  }
};
