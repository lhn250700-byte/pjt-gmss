# ![로고(마크)](https://crrxqwzygpifxmzxszdz.supabase.co/storage/v1/object/public/site_img/h_logo.png) AI 및 전문 상담 시스템 고민순삭 (GMSS)

**제작 기간**: 2026-01-07 ~ 2026-03-17

## 👥Who are we?

### 이하늘 (팀장)

- 상담신청
- 커뮤니티 월간/주간/실시간/개인맞춤형 추천순
- 로그인(소셜 포함) / 회원가입 / 소셜 로그인
- 상담 수락 / 거절
- admin 및 counselor 페이지
- 프로젝트 일정 관리
- 토스페이먼츠 API 연동

### 박종석

- DB 설계
- Query문 작성

### 임윤섭

- 로고 제작
- 홈페이지 디자인
- Entity 설계
- 영상 제작
- PPT 제작

### 김태길

- 챗봇, AI 상담, 화상 상담, 채팅 상담
- 홈페이지 디자인
- CI/CD

### 강성진

- 게시판, 댓글 CRUD
- 카카오맵
- 좋아요/싫어요
- 민감 키워드 감지
- 홈페이지 디자인 구현


## Overview

고민순삭은 현대 사회의 20~40대가 겪는 진로, 직장, 인간관계 등 다양한 고민을 보다 쉽고 빠르게 해소할 수 있도록 돕는 AI 기반 상담 플랫폼이다. 많은 사람들이 시간적 제약과 심리적 부담으로 인해 상담을 망설이는 현실에서, 고민순삭은 언제 어디서나 부담 없이 자신의 이야기를 털어놓을 수 있는 환경을 제공하는 것을 목표로 한다.

특히 기존 상담 서비스의 낮은 접근성과 초기 고민 해결의 한계를 보완하기 위해, AI 상담을 통해 사용자가 자신의 문제를 정리하고 정서적인 지원을 받을 수 있도록 설계되었다. 더 나아가 필요 시 전문가 상담 및 커뮤니티와의 연계를 통해 단계적으로 심층적인 도움을 받을 수 있는 통합 상담 경험을 제공한다.

---

## 문제 인식

고민 상담 서비스는 다양하게 존재하지만, 사용자가 실제로 겪는 가장 큰 문제는 “어디서부터 시작해야 하는지”와 “상담 이후 무엇을 남길 수 있는지”입니다.

익명 기반 커뮤니티는 접근성이 높지만, 자해나 자살과 같은 민감한 이슈가 함께 존재하며 이를 안전하게 관리할 수 있는 체계가 부족합니다.

또한 상담 이후 기록이 남지 않거나 맥락이 단절되는 경우가 많아, 사용자는 같은 고민을 반복적으로 작성하거나 설명해야 하는 비효율을 겪습니다. 이로 인해 서비스 신뢰도와 재이용 경험이 저하되는 문제가 발생합니다.

---

## 해결 방안

고민순삭은 게시판 → 상담 예약 → 실시간 상담 → 요약 및 기록으로 이어지는 구조를 통해 고민의 흐름이 끊기지 않도록 설계된 서비스입니다.

AI 상담을 통해 즉시 상담을 시작할 수 있도록 하여 초기 진입 장벽을 낮추고, 필요 시 상담사 상담으로 자연스럽게 연결됩니다.

또한 상담 종료 후 요약 및 기록을 저장하여 사용자가 자신의 고민을 정리하고 이후에도 지속적으로 활용할 수 있도록 하였습니다.

더불어 민감 키워드 감지 기반의 위험 케이스 관리 기능을 통해 커뮤니티의 안전성과 서비스 신뢰도를 확보하고자 했습니다.

---

## 🛠️ 기술 스택

<div>

<img src="https://img.shields.io/badge/React-61DAFB?style=flat-square&logo=react&logoColor=white"/>
<img src="https://img.shields.io/badge/Vite-646CFF?style=flat-square&logo=vite&logoColor=white"/>
<img src="https://img.shields.io/badge/React_Router-CA4245?style=flat-square&logo=reactrouter&logoColor=white"/>
<img src="https://img.shields.io/badge/Tailwind_CSS-06B6D4?style=flat-square&logo=tailwindcss&logoColor=white"/>
<img src="https://img.shields.io/badge/Zustand-000000?style=flat-square"/>
<img src="https://img.shields.io/badge/Axios-5A29E4?style=flat-square&logo=axios&logoColor=white"/>
<img src="https://img.shields.io/badge/PeerJS-FF6B6B?style=flat-square"/>
<img src="https://img.shields.io/badge/Supabase-3ECF8E?style=flat-square&logo=supabase&logoColor=white"/>
<img src="https://img.shields.io/badge/STOMP-000000?style=flat-square"/>
<img src="https://img.shields.io/badge/SockJS-000000?style=flat-square"/>
<img src="https://img.shields.io/badge/TossPayments-0064FF?style=flat-square"/>
<img src="https://img.shields.io/badge/Kakao_Maps-FFCD00?style=flat-square"/>

<br/>

<img src="https://img.shields.io/badge/Spring_Boot-6DB33F?style=flat-square&logo=springboot&logoColor=white"/>
<img src="https://img.shields.io/badge/Spring_Security-6DB33F?style=flat-square&logo=springsecurity&logoColor=white"/>
<img src="https://img.shields.io/badge/JWT-000000?style=flat-square&logo=jsonwebtokens&logoColor=white"/>
<img src="https://img.shields.io/badge/OAuth2-4285F4?style=flat-square"/>
<img src="https://img.shields.io/badge/WebSocket-010101?style=flat-square&logo=websocket"/>
<img src="https://img.shields.io/badge/FastAPI-009688?style=flat-square&logo=fastapi&logoColor=white"/>
<img src="https://img.shields.io/badge/PostgreSQL-4169E1?style=flat-square&logo=postgresql&logoColor=white"/>

</div>

### Frontend 
- React 19.2.0 
- Vite 7.2.4 
- React Router DOM 7.13.1 
- Tailwind CSS 4.1.18 
- Zustand 5.0.11
- Axios 1.13.2 
- PeerJS 1.5.5 
- Supabase JS 2.93.3 
- STOMP / SockJS 
- TossPayments SDK 2.5.0 
- Kakao Maps SDK 1.2.0 

### Backend / AI
- Spring Boot 3.x (Spring Security, JWT, OAuth2)
- WebSocket (STOMP)
- FastAPI (AI 상담 / 요약 / 추천)
- PostgreSQL 15 (Supabase)

---

## 📋 주요 기능

### 1. 직관적인 사용자 경험
헤더 및 하단 네비게이션을 통해 사용자, 상담사, 관리자 역할별 동선을 분리하고 주요 기능에 빠르게 접근할 수 있도록 구성했습니다.

### 2. 회원 및 인증
JWT 기반 인증과 토큰 갱신을 통해 로그인 상태를 유지하며, 사용자 역할(USER / SYSTEM / ADMIN)에 따라 접근 가능한 기능을 제어합니다.

### 3. AI 상담
즉시 상담 생성 후 대화를 진행할 수 있으며, 상담 종료 시 자동 요약을 생성하여 기록으로 저장합니다.

상담 시작 전 안내 및 동의 UI를 제공하고, 포인트 차감 기반으로 이용 흐름을 구성했습니다.

### 4. 상담사 찾기 및 예약
상담사 목록, 상세 정보, 리뷰를 확인할 수 있으며 예약 가능한 시간 조회 및 상담 예약 기능을 제공합니다.

### 5. 실시간 채팅 및 화상 상담
PeerJS 기반 1:1 화상 상담을 제공하며, 상담 중 채팅 기능을 함께 지원합니다.

상담 종료 후 녹화 다운로드 및 STT 기반 요약 기능을 통해 대화 내용을 정리하고 저장합니다.

### 6. 커뮤니티 게시판
공지, 자유, MBTI 게시판을 제공하며 검색, 좋아요/싫어요, 댓글 기능을 지원합니다.

실시간 인기글, 주간/월간 인기글, 사용자 기반 추천 게시글 기능을 제공합니다.

### 7. 위험 키워드 감지
민감 키워드를 기반으로 위험 게시글을 감지하고, 상담사 전용 화면에서 해당 게시글을 확인하고 대응할 수 있도록 구성했습니다.

### 8. 마이페이지
회원 정보 수정, 상담 내역(진행/예약/완료), 작성 글 및 댓글, 포인트 충전 및 사용 내역을 확인할 수 있습니다.

### 9. 관리자 기능
상담 건수, 매출, 수수료, 정산 등의 통계 대시보드를 제공하며, 민감 키워드 관리 및 위험 감지 현황을 확인할 수 있습니다.

### 10. 플로팅 챗봇 ‘순삭이’
서비스 이용 방법을 안내하는 챗봇을 제공하며, 대화 세션과 요약을 저장합니다.

또한 상담 알림(진행 예정/진행 중)과 연동되어 사용자 경험을 보조합니다.
