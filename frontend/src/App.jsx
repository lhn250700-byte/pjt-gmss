import React from 'react';
import { Route, Routes } from 'react-router-dom';
import Home from './pages/common/home/Home';
import Chat from './pages/user/chat/Chat';
import Board from './pages/user/board/Board';
import Info from './pages/user/info/Info';
import Member from './pages/common/member/Member';
import MyPage from './pages/common/mypage/MyPage';
import ProtectedRoute from './components/ProtectedRoute';
import Alarm from './pages/admin/Alarm';
import Statistics from './pages/admin/Statistics';
import EditInfo from './pages/system/info/EditInfo';
import MyCounsel from './pages/system/info/MyCounsel';
import About from './pages/system/info/About';

const App = () => {
  return (
    <Routes>
      {/* COMMON */}
      {/* HOME */}
      <Route path="/" element={<Home />} />
      {/* MEMBER */}
      <Route path="/member/*" element={<Member />} />
      {/* MY PAGE */}
      <Route path="/mypage/*" element={<MyPage />} />

      {/* USER */}
      {/* CHAT */}
      <Route
        path="/chat/*"
        element={
          <ProtectedRoute allowRoles={['USER']}>
            <Chat />
          </ProtectedRoute>
        }
      />
      {/* BOARD */}
      <Route
        path="/board/*"
        element={
          <ProtectedRoute allowRoles={['USER']}>
            <Board />
          </ProtectedRoute>
        }
      />
      {/* INFO */}
      <Route
        path="/info/*"
        element={
          <ProtectedRoute allowRoles={['USER']}>
            <Info />
          </ProtectedRoute>
        }
      />

      {/* SYSTEM */}
      {/* EDITINFO */}
      <Route
        path="editinfo"
        element={
          <ProtectedRoute allowRoles={['SYSTEM']}>
            <EditInfo />
          </ProtectedRoute>
        }
      />
      {/* COUNSEL HISTORY */}
      <Route
        path="mycounsel"
        element={
          <ProtectedRoute allowRoles={['SYSTEM']}>
            <MyCounsel />
          </ProtectedRoute>
        }
      />
      {/* PROFILE */}
      <Route
        path="about/*"
        element={
          <ProtectedRoute allowRoles={['SYSTEM']}>
            <About />
          </ProtectedRoute>
        }
      />

      {/* ADMIN */}
      {/* ALARM */}
      <Route
        path="/alarm/*"
        element={
          <ProtectedRoute allowRoles={['ADMIN']}>
            <Alarm />
          </ProtectedRoute>
        }
      />
      {/* STATS */}
      <Route
        path="/stats/*"
        element={
          <ProtectedRoute allowRoles={['ADMIN']}>
            <Statistics />
          </ProtectedRoute>
        }
      />
    </Routes>
  );
};

export default App;
