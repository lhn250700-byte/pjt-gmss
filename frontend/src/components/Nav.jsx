import React from 'react';
import { NavLink } from 'react-router-dom';
import useAuth from '../hooks/useAuth';

const Nav = () => {
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
    <>
      {/* 아이콘 필요하면 lucide-react install 후, 아이콘 사용 */}
      <nav className="lg:hidden text-main-02 bg-white fixed w-full inset-x-0 bottom-0 border-t">
        <div className="w-full md:w-[90%] mx-auto px-4">
          <ul className="flex h-16">
            {MENUS.map(({ label, to }) => (
              <li key={to} className="flex-1">
                <NavLink
                  to={to}
                  className={({ isActive }) =>
                    `flex h-full items-center justify-center pb-2 text-sm transition-colors
                 ${isActive ? 'font-semibold text-blue-600' : 'text-gray-500'}`
                  }
                >
                  {label}
                </NavLink>
              </li>
            ))}
          </ul>
        </div>
      </nav>
    </>
  );
};

export default Nav;
