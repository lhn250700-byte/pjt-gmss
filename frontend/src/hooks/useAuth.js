export default function useAuth() {
  // 지금은 가짜
  const user = {
    isLogin: true,
    role: 'ADMIN', // USER | SYSTEM | ADMIN
  };

  return { user };
}
