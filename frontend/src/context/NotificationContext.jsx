import { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { useAuth } from './AuthContext';
import apiClient from '../services/apiClient';
import { API_ENDPOINTS } from '../constants/apiEndpoints';

const defaultNotificationContext = {
  unreadCount: 0,
  refreshUnreadCount: async () => {},
  decrementUnread: () => {},
  clearUnread: () => {},
};

const NotificationContext = createContext(defaultNotificationContext);

export function NotificationProvider({ children }) {
  const { auth, loading: authLoading } = useAuth();
  const [unreadCount, setUnreadCount] = useState(0);

  const refreshUnreadCount = useCallback(async () => {
    if (authLoading || !auth?.token) {
      if (!auth?.token) {
        setUnreadCount(0);
      }
      return;
    }

    try {
      apiClient.setToken(auth.token);
      const count = await apiClient.get(`${API_ENDPOINTS.NOTIFICATIONS}/unread-count`);
      setUnreadCount(typeof count === 'number' ? count : Number(count) || 0);
    } catch (err) {
      console.error('Failed to fetch unread count:', err);
    }
  }, [auth?.token, authLoading]);

  useEffect(() => {
    if (!authLoading) {
      refreshUnreadCount();
    }
  }, [authLoading, refreshUnreadCount]);

  useEffect(() => {
    if (authLoading || !auth?.token) return;
    const id = setInterval(refreshUnreadCount, 60000);
    return () => clearInterval(id);
  }, [auth?.token, authLoading, refreshUnreadCount]);

  useEffect(() => {
    if (authLoading || !auth?.token) return;
    const onFocus = () => refreshUnreadCount();
    window.addEventListener('focus', onFocus);
    return () => window.removeEventListener('focus', onFocus);
  }, [auth?.token, authLoading, refreshUnreadCount]);

  const decrementUnread = (by = 1) => {
    setUnreadCount((c) => Math.max(0, c - by));
  };

  const clearUnread = () => setUnreadCount(0);

  return (
    <NotificationContext.Provider
      value={{ unreadCount, refreshUnreadCount, decrementUnread, clearUnread }}
    >
      {children}
    </NotificationContext.Provider>
  );
}

export function useNotifications() {
  return useContext(NotificationContext);
}
