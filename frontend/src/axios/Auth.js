import axios from 'axios';
import { useAuthStore } from '../store/auth.store';

export const authApi = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  withCredentials: true, // refreshToken cookie
});

authApi.interceptors.request.use((config) => {
  const accessToken = useAuthStore.getState().accessToken;

  if (accessToken) {
    config.headers.Authorization = `Bearer ${accessToken}`;
    config.headers['Content-Type'] = 'application/json';
  }

  return config;
});

export const refreshAccessToken = async () => {
  const { setAccessToken, setLoginStatus, setEmail, clearAuth } =
    useAuthStore.getState();

  try {
    const accessToken = useAuthStore.getState().accessToken;
    const res = await authApi.post('/api/auth/refresh', null, {
      headers: accessToken ? { Authorization: `Bearer ${accessToken}` } : {},
    });
    const data = res.data;
    console.log('리플래쉬 자료 전송 완료');
    console.log(data);

    setAccessToken(data.accessToken);
    setLoginStatus(true);
    setEmail(data.email);
  } catch (error) {
    console.error('토큰 갱신 실패 : ', error);
    clearAuth();
    return null;
  }
};
