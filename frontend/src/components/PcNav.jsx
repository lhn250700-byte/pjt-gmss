import React from 'react';
import { NavLink } from 'react-router-dom';
import useAuth from '../hooks/useAuth';

const PcNav = () => {
  let MENUS = [];
  const { user } = useAuth();

  if (user.role === 'USER') {
    MENUS.push(
      { label: '홈', to: '/' },
      { label: '채팅', to: '/chat' },
      { label: '게시판', to: '/board' },
      { label: 'INFO', to: '/info' },
      { label: '로그인', to: '/member/signin' },
    );
  } else if (user.role === 'ADMIN') {
    MENUS.push(
      { label: '대시보드', to: '/' },
      { label: '알림', to: '/alarm' },
      { label: '통계자료', to: '/stats' },
      { label: '로그인', to: '/member/signin' },
    );
  } else return;

  return (
    <nav className="hidden lg:block w-full bg-white top-0 left-0 z-50 shadow-lg">
      <div className="w-full md-[90%] mx-auto px-4 container">
        <div className="flex items-center justify-between h-16">
          {/* 로고 영역 */}
          <div className="text-lg font-bold text-blue-600">LOGO</div>

          {/* 메뉴 */}
          <ul className="flex gap-8">
            {MENUS.map(({ label, to }) => (
              <li key={to}>
                <NavLink
                  to={to}
                  className={({ isActive }) =>
                    `text-sm transition-colors
                  ${isActive ? 'font-semibold text-blue-600' : 'text-gray-600 hover:text-blue-600'}`
                  }
                >
                  {label}
                </NavLink>
              </li>
            ))}
          </ul>
        </div>
      </div>
    </nav>
  );
};

export default PcNav;
