import axios from 'axios';
import { BASE_URL } from './config';
import { authApi } from '../axios/Auth';

// 현재 내 잔액 가져오기
export const getMyPoint = async (email) => {
  const { data } = await authApi.get('/api/wallet_getpoint', {
    params: {
      email,
    },
  });

  return data;
};
