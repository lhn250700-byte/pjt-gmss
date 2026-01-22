import React from 'react';
import { Route, Routes } from 'react-router-dom';
import CounselorList from './CounselorList';
import CounselorView from './CounselorView';

const Counselor = () => {
  return (
    <Routes>
      <Route index element={<CounselorList />} />
      <Route path="/:c_id" element={<CounselorView />} /> {/* http://localhost:5173/chat/counselor/view/2 */}
    </Routes>
  );
};

export default Counselor;
