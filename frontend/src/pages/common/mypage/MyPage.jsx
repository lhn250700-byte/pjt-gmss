import React from 'react';
import EditInfo from './EditInfo';
import EditInfo_Counselor from '../../system/info/EditInfo';
import CounselList from './CounselList';
import { Route, Routes } from 'react-router-dom';
import MyPost from './MyPost';
import MyComment from './MyComment';
import useAuth from '../../../hooks/useAuth';
import MyCounsel from '../../system/info/MyCounsel';
import About from '../../system/info/About';
import UserDefaultPage from './UserDefaultPage';
import CounselorDefaultPage from '../../system/info/CounselorDefaultPage';
import DashBoard from '../../admin/DashBoard';
import Alarm from '../../admin/Alarm';
import Statistics from '../../admin/Statistics';
import Admin from '../../admin/Admin';

const MyPage = () => {
  const { user } = useAuth();
  if (user.role === 'USER') {
    // js 코드 작성란

    return (
      <>
        <Routes>
          <Route index element={<UserDefaultPage />} />
          <Route path="editinfo" element={<EditInfo />} />
          <Route path="clist" element={<CounselList />} />
          <Route path="postlist" element={<MyPost />} />
          <Route path="commentlist" element={<MyComment />} />
        </Routes>
      </>
    );
  } else if (user.role === 'SYSTEM') return;
  else {
    // js 코드 작성란

    return (
      <Routes>
        <Route index element={<Admin />} />
      </Routes>
    );
  }
};

export default MyPage;
