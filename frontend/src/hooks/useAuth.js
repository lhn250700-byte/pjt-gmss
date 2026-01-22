export default function useAuth() {
  // 지금은 가짜
  const user = {
    isLogin: true,
    role: 'USER', // USER | SYSTEM | ADMIN
  };

  return { user };
}
