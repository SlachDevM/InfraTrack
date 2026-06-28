import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { canAccessRoute } from '../constants/navigation';
import ForbiddenPage from '../pages/ForbiddenPage';

export default function PrivateRoute({ children }) {
  const { auth, loading } = useAuth();
  const location = useLocation();

  if (loading) {
    return <div className="loading">Loading...</div>;
  }

  if (!auth) {
    return <Navigate to="/login" />;
  }

  if (!canAccessRoute(auth.user?.role, location.pathname)) {
    return <ForbiddenPage />;
  }

  return children;
}
