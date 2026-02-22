import { create } from 'zustand';

export const useAuthStore = create((set) => ({
  accessToken: null,
  email: null,
  // userInfo:{username:null,email:null,role:null,img:null},
  loginStatus: false,
  setAccessToken: (accessToken) => set({ accessToken }),
  setEmail: (email) => set({ email }),
  setLoginStatus: (loginStatus) => set({ loginStatus }),
  clearAuth: () => set({ accessToken: null, email: null, loginStatus: false }),
  // setUserInfo:(userInfo)=>set({userInfo})
}));
