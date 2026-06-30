import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import AppNavbar from '../components/navigation/AppNavbar';
import { canViewOperationsDashboard, getRoleLabel } from '../constants/userRoles';
import {
  FIELD_EMPLOYEE_SHORTCUTS,
  isFieldEmployeeRole,
} from '../constants/navigation';
import { APP_VERSION } from '../config/appVersion';
import '../styles/PlatformShell.css';

export default function PlatformShell() {
  const navigate = useNavigate();
  const { auth, logout } = useAuth();
  const fieldEmployee = isFieldEmployeeRole(auth?.user?.role);

  useEffect(() => {
    if (canViewOperationsDashboard(auth?.user?.role)) {
      navigate('/dashboard', { replace: true });
    }
  }, [auth?.user?.role, navigate]);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  if (canViewOperationsDashboard(auth?.user?.role)) {
    return <div className="loading">Loading dashboard...</div>;
  }

  return (
    <div className="platform-shell">
      <AppNavbar onNavigate={navigate} onLogout={handleLogout} />

      <div className="platform-content">
        <div className="platform-welcome">
          <h1>InfraTrack</h1>
          <p>Operational asset and field operations management platform.</p>

          {fieldEmployee ? (
            <div className="platform-info">
              <h2>Your Work</h2>
              <p>Select a task below to continue your assigned operational work.</p>
              <ul className="field-employee-shortcuts">
                {FIELD_EMPLOYEE_SHORTCUTS.map((shortcut) => (
                  <li key={shortcut.path}>
                    <button
                      type="button"
                      className="shortcut-link"
                      onClick={() => navigate(shortcut.path)}
                    >
                      {shortcut.label}
                    </button>
                  </li>
                ))}
              </ul>
              <div className="user-info">
                <p>
                  Logged in as:
                  {' '}
                  <strong>{auth?.user?.email}</strong>
                  {' '}
                  (
                  {getRoleLabel(auth?.user?.role)}
                  )
                </p>
              </div>
            </div>
          ) : (
            <div className="platform-info">
              <h2>Getting Started</h2>
              <p>
                The platform provides infrastructure for authentication, user management,
                notifications, and deployment while business domain features are implemented incrementally.
              </p>

              <div className="feature-section">
                <h3>Platform Features</h3>
                <ul>
                  <li>User authentication and role-based access control</li>
                  <li>User management with activation workflow</li>
                  <li>Notification system with Firebase integration</li>
                  <li>Email infrastructure for production</li>
                  <li>Production-ready Docker configuration</li>
                  <li>PostgreSQL database</li>
                </ul>
              </div>

              <div className="feature-section">
                <h3>Next Steps</h3>
                <p>
                  Business capabilities are implemented one use case at a time. See the documentation for the current development roadmap.
                </p>
                <ul>
                  <li>Backend domain code in <code>backend/src/main/java/com/infratrack/</code></li>
                  <li>React pages in <code>frontend/src/pages/</code></li>
                  <li>API services in <code>frontend/src/services/</code></li>
                </ul>
              </div>

              <div className="feature-section">
                <h3>Documentation</h3>
                <p>
                  See the <code>docs/</code> directory for:
                </p>
                <ul>
                  <li>Business Discovery</li>
                  <li>Functional Use Cases</li>
                  <li>System Blueprint</li>
                </ul>
              </div>

              <div className="user-info">
                <p>
                  Logged in as:
                  {' '}
                  <strong>{auth?.user?.email}</strong>
                  {' '}
                  (
                  {getRoleLabel(auth?.user?.role)}
                  )
                </p>
              </div>
            </div>
          )}
        </div>
      </div>

      <footer className="platform-footer">
        InfraTrack v
        {APP_VERSION}
      </footer>
    </div>
  );
}
