import React from 'react';
import useAuth from '../../../hooks/useAuth';
import DashBoard from '../../admin/DashBoard';
import CounselorDefaultPage from '../../system/info/CounselorDefaultPage';

const Home = () => {
  const { user } = useAuth();

  if (user.role === 'ADMIN') return <DashBoard />;
  else if (user.role === 'USER') return <div>Home</div>;
  else return <CounselorDefaultPage />;
};

export default Home;
