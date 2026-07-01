import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useNotifications } from '../context/NotificationContext';
import apiClient from '../services/apiClient';
import notificationApi from '../services/notificationApi';
import PaginationControls from '../components/PaginationControls';
import { getApiErrorMessage } from '../utils/apiError';
import { ROUTES } from '../constants/routes';
import {
  DEFAULT_PAGE,
  getPageNumber,
  getTotalPages,
  unwrapPageContent,
} from '../utils/pagination';
import '../styles/NotificationPage.css';

export default function NotificationPage() {
  const navigate = useNavigate();
  const { auth, logout } = useAuth();
  const [notifications, setNotifications] = useState([]);
  const [notificationsPage, setNotificationsPage] = useState(DEFAULT_PAGE);
  const [notificationsTotalPages, setNotificationsTotalPages] = useState(0);
  const [listLoading, setListLoading] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const { decrementUnread, clearUnread, refreshUnreadCount } = useNotifications();

  useEffect(() => {
    if (!auth?.token) {
      navigate(ROUTES.LOGIN);
      return;
    }
    apiClient.setToken(auth.token);
    fetchNotifications();
    refreshUnreadCount();
  }, [auth?.token, navigate, refreshUnreadCount]);

  const fetchNotifications = async (page = notificationsPage) => {
    try {
      setListLoading(true);
      setError(null);
      const notificationPage = await notificationApi.list(page);
      setNotifications(unwrapPageContent(notificationPage));
      setNotificationsPage(getPageNumber(notificationPage, page));
      setNotificationsTotalPages(getTotalPages(notificationPage));
    } catch (err) {
      setError(getApiErrorMessage(err, 'Failed to load notifications.'));
    } finally {
      setLoading(false);
      setListLoading(false);
    }
  };

  const handleMarkAsRead = async (notificationId) => {
    try {
      await notificationApi.markAsRead(notificationId);
      const wasUnread = notifications.some((n) => n.id === notificationId && !n.isRead);
      setNotifications((prev) =>
        prev.map((n) => (n.id === notificationId ? { ...n, isRead: true } : n))
      );
      if (wasUnread) {
        decrementUnread();
      }
      return true;
    } catch (err) {
      console.error('Failed to mark notification as read:', err);
      return false;
    }
  };

  const handleMarkAllAsRead = async () => {
    try {
      await notificationApi.markAllAsRead();
      clearUnread();
      fetchNotifications(notificationsPage);
    } catch (err) {
      console.error('Failed to mark all as read:', err);
    }
  };

  const handleNotificationClick = async (notif) => {
    if (!notif.isRead) {
      await handleMarkAsRead(notif.id);
    }

    if (notif.targetRoute) {
      navigate(notif.targetRoute);
    }
  };

  const handleLogout = () => {
    logout();
    navigate(ROUTES.LOGIN);
  };

  if (loading) {
    return <div className="loading">Loading notifications...</div>;
  }

  return (
    <div className="notification-page">
      <header className="notification-header">
        <button className="back-btn" onClick={() => navigate(ROUTES.HOME)}>
          ← Back to Dashboard
        </button>
        <h1>Notifications</h1>
        {notifications.length > 0 && (
          <button className="mark-all-btn" onClick={handleMarkAllAsRead}>
            Mark All as Read
          </button>
        )}
        <button className="logout-btn" onClick={handleLogout}>
          Logout
        </button>
      </header>

      <main className="notification-content">
        {error ? (
          <div className="notification-error">{error}</div>
        ) : notifications.length === 0 ? (
          <div className="no-notifications">
            <p>You have no notifications.</p>
          </div>
        ) : (
          <div className="notification-list">
            {notifications.map((notif) => (
              <div
                key={notif.id}
                className={`notification-item ${notif.isRead ? 'read' : 'unread'} clickable`}
                onClick={() => handleNotificationClick(notif)}
                onKeyDown={(e) => {
                  if (e.key === 'Enter' || e.key === ' ') {
                    e.preventDefault();
                    handleNotificationClick(notif);
                  }
                }}
                role="button"
                tabIndex={0}
              >
                <div className="notification-content">
                  <div className="notification-header-row">
                    {notif.title && (
                      <span className="notification-title">{notif.title}</span>
                    )}
                    <span className="notification-time">
                      {new Date(notif.createdAt).toLocaleString()}
                    </span>
                  </div>
                  <p className="notification-message">{notif.message}</p>
                </div>
              </div>
            ))}
          </div>
        )}
        <PaginationControls
          page={notificationsPage}
          totalPages={notificationsTotalPages}
          loading={listLoading}
          onPrevious={() => fetchNotifications(notificationsPage - 1)}
          onNext={() => fetchNotifications(notificationsPage + 1)}
        />
      </main>
    </div>
  );
}
