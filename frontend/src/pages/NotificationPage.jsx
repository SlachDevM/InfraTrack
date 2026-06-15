import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import '../styles/NotificationPage.css';

const API_BASE = 'http://localhost:4000';

export default function NotificationPage() {
  const navigate = useNavigate();
  const { auth, logout } = useAuth();
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!auth) {
      navigate('/login');
      return;
    }
    fetchNotifications();
  }, [auth, navigate]);

  const fetchNotifications = async () => {
    try {
      const response = await fetch(`${API_BASE}/api/notifications`, {
        headers: { Authorization: `Bearer ${auth.token}` },
      });
      const data = await response.json();
      setNotifications(data);
    } catch (err) {
      console.error('Failed to fetch notifications:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleMarkAsRead = async (notificationId) => {
    try {
      const response = await fetch(`${API_BASE}/api/notifications/${notificationId}/read`, {
        method: 'PUT',
        headers: { Authorization: `Bearer ${auth.token}` },
      });

      if (response.ok) {
        setNotifications(
          notifications.map((n) =>
            n.id === notificationId ? { ...n, isRead: true } : n
          )
        );
      }
    } catch (err) {
      console.error('Failed to mark notification as read:', err);
    }
  };

  const handleMarkAllAsRead = async () => {
    try {
      const response = await fetch(`${API_BASE}/api/notifications/read-all`, {
        method: 'PUT',
        headers: { Authorization: `Bearer ${auth.token}` },
      });

      if (response.ok) {
        fetchNotifications();
      }
    } catch (err) {
      console.error('Failed to mark all as read:', err);
    }
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  if (loading) {
    return <div className="loading">Loading notifications...</div>;
  }

  return (
    <div className="notification-page">
      <header className="notification-header">
        <button className="back-btn" onClick={() => navigate('/')}>
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
        {notifications.length === 0 ? (
          <div className="no-notifications">
            <p>You have no notifications.</p>
          </div>
        ) : (
          <div className="notification-list">
            {notifications.map((notif) => (
              <div
                key={notif.id}
                className={`notification-item ${notif.isRead ? 'read' : 'unread'}`}
              >
                <div className="notification-content">
                  <div className="notification-header-row">
                    <span className="notification-type">{notif.type}</span>
                    <span className="notification-time">
                      {new Date(notif.createdAt).toLocaleString()}
                    </span>
                  </div>
                  <p className="notification-message">{notif.message}</p>
                </div>
                {!notif.isRead && (
                  <button
                    className="mark-read-btn"
                    onClick={() => handleMarkAsRead(notif.id)}
                  >
                    Mark as Read
                  </button>
                )}
              </div>
            ))}
          </div>
        )}
      </main>
    </div>
  );
}
