import { Navigate } from 'react-router-dom';
import useAuth from '../hooks/useAuth';

export default function ProtectedRoute({ children, allowRoles }) {
  const { user } = useAuth();

  if (!user || !user.isLogin) return <Navigate to="/member/signin" replace />;

  if (allowRoles && !allowRoles.includes(user.role)) {
    return <Navigate to="/" replace />;
  }

  return children;
}
