import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import apiClient from '../services/apiClient';
import { registerUnauthorizedHandler } from '../services/unauthorizedHandler';
import { ROUTES } from '../constants/routes';

export default function ApiAuthBridge() {
  const { auth, logout, loading } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    registerUnauthorizedHandler(() => {
      logout();
      apiClient.setToken(null);
      navigate(ROUTES.LOGIN, { state: { sessionExpired: true }, replace: true });
    });

    return () => registerUnauthorizedHandler(null);
  }, [logout, navigate]);

  useEffect(() => {
    if (loading) {
      return;
    }
    apiClient.setToken(auth?.token ?? null);
  }, [auth, loading]);

  return null;
}
