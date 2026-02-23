import axios from 'axios';
import { BASE_URL } from './config';

// [실시간 인기글]
export const getRealtimePopularPosts = async () => {
  try {
    const { data } = await axios.get(`${BASE_URL}/posts/popular/realtime`);

    return data;
  } catch (error) {
    console.error('getRealtimePopularPosts error:', error);
    throw error;
  }
};

// [주간 인기글]
export const getWeeklyPopularPosts = async () => {
  try {
    const { data } = await axios.get(`${BASE_URL}/posts/popular/weekly`);

    return data;
  } catch (error) {
    console.error('getWeeklyPopularPosts error:', error);
    throw error;
  }
};
