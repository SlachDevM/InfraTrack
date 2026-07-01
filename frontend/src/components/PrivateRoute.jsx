import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { canAccessRoute } from '../constants/navigation';
import { ROUTES } from '../constants/routes';
import { COMMON_LABELS } from '../constants/uiLabels';
import ForbiddenPage from '../pages/ForbiddenPage';

export default function PrivateRoute({ children }) {
  const { auth, loading } = useAuth();
  const location = useLocation();

  if (loading) {
    return <div className="loading">{COMMON_LABELS.LOADING}</div>;
  }

  if (!auth) {
    return <Navigate to={ROUTES.LOGIN} />;
  }

  if (!canAccessRoute(auth.user?.role, location.pathname)) {
    return <ForbiddenPage />;
  }

  return children;
}
