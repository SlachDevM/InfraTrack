import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useNotifications } from '../context/NotificationContext';
import { ROUTES } from '../constants/routes';
import { DISPLAY_LIMITS } from '../constants/limits';
import '../styles/Dashboard.css';

export default function NotificationButton() {
  const navigate = useNavigate();
  const { unreadCount, refreshUnreadCount } = useNotifications();

  useEffect(() => {
    refreshUnreadCount();
  }, [refreshUnreadCount]);

  return (
    <button
      type="button"
      className="notification-btn"
      onClick={() => navigate(ROUTES.NOTIFICATIONS)}
      aria-label={
        unreadCount > 0
          ? `Notifications, ${unreadCount} unread`
          : 'Notifications'
      }
    >
      🔔 Notifications
      {unreadCount > 0 && (
        <span className="notification-badge">
          {unreadCount > DISPLAY_LIMITS.NOTIFICATION_BADGE_CAP
            ? `${DISPLAY_LIMITS.NOTIFICATION_BADGE_CAP}+`
            : unreadCount}
        </span>
      )}
    </button>
  );
}
