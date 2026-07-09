import { useMemo } from 'react';
import { useAuth } from '../../context/AuthContext';
import NotificationButton from '../NotificationButton';
import NavigationMoreMenu from './NavigationMoreMenu';
import { canManageUsers } from '../../constants/userRoles';
import { ROUTES } from '../../constants/routes';
import { COMMON_LABELS } from '../../constants/uiLabels';
import { getOverflowNavigationItems, getPrimaryNavigationItems } from '../../constants/navigation';

export default function AppNavbar({ onNavigate, onLogout }) {
  const { auth } = useAuth();

  const primaryNavigationItems = getPrimaryNavigationItems(auth?.user?.role);
  const overflowNavigationItems = useMemo(() => {
    const items = getOverflowNavigationItems(auth?.user?.role);
    if (canManageUsers(auth?.user?.role)) {
      return [...items, { path: ROUTES.USERS, label: 'Users' }];
    }
    return items;
  }, [auth?.user?.role]);

  return (
    <nav className="platform-navbar">
      <div className="navbar-brand">InfraTrack</div>
      <div className="navbar-items">
        {primaryNavigationItems.map((item) => (
          <button
            key={item.path}
            type="button"
            className="navbar-link"
            onClick={() => onNavigate(item.path)}
          >
            {item.label}
          </button>
        ))}
        <NavigationMoreMenu items={overflowNavigationItems} onNavigate={onNavigate} />
        <NotificationButton />
        <button type="button" className="logout-btn" onClick={onLogout} aria-label="Log out">
          {COMMON_LABELS.LOGOUT}
        </button>
      </div>
    </nav>
  );
}
