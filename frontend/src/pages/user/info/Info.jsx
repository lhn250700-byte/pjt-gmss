import React from 'react';
import { Route, Routes } from 'react-router-dom';
import About from './About';
import DocumentGuide from './DocumentGuide';
import InterviewGuide from './InterviewGuide';
import Map from './Map';

const Info = () => {
  return (
    <Routes>
      <Route index element={<About />} />
      <Route path="d_guide" element={<DocumentGuide />} />
      <Route path="i_guide" element={<InterviewGuide />} />
      <Route path="map" element={<Map />} />
    </Routes>
  );
};

export default Info;
