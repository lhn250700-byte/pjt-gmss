import React from 'react';
import { Link } from 'react-router-dom';

const Footer = () => {
  return (
    <footer className="bg-deep text-white py-8 md:py-12">
      <div className="container">
        <div className="flex flex-col md:justify-between gap-4">
          {/* 로고 */}
          <Link to="/" className="block hover:opacity-80 transition-opacity duration-300 w-fit">
            {/* <img
              src="https://ocnuykfvdtebmondqppu.supabase.co/storage/v1/object/public/images/logo_wh.png"
              alt="logo"
              className="h-10 w-auto"
            /> */}
          </Link>

          {/* 설명 */}
          <div className="text-white text-xs xs:text-center lg:text-left">
            <p>
              <strong>AI 융합 고민 상담 서비스</strong>
            </p>
            <p style={{ fontWeight: '300' }}> 고민, 커리어, 취업 까지 혼자 고민하지 마세요.</p>
            <p className="text-[10px]!" style={{ fontWeight: '300' }}>
              ※ 고민순삭의 AI상담은 법적, 정신과적 진단의 처방을 대체하지 않습니다.
            </p>
          </div>
          <hr style={{ color: '#969696' }} />
          <div className="text-white text-xs xs:text-center lg:text-left">
            <p style={{ fontWeight: '300' }}>AI 상담은 참고용으로 제공되며,</p>
            <p style={{ fontWeight: '300' }}>
              긴급 상황시 <strong style={{ fontWeight: '700' }}>***109(자살 예방 상담전화)***</strong>에 문의 하시기
              바랍니다.
            </p>
          </div>
          <hr style={{ color: '#969696' }} />
          <div className="text-white text-xs xs:text-center lg:text-left">
            <p style={{ fontWeight: '300' }}>ⓒ 2026 고민순삭. All rights reserved.</p>
          </div>
        </div>
      </div>
    </footer>
  );
};

export default Footer;
