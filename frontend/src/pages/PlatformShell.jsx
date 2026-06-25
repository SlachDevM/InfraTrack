import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import NotificationButton from '../components/NotificationButton';
import { canManageUsers, getRoleLabel } from '../constants/userRoles';
import '../styles/PlatformShell.css';

export default function PlatformShell() {
  const navigate = useNavigate();
  const { auth, logout } = useAuth();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="platform-shell">
      <nav className="platform-navbar">
        <div className="navbar-brand">InfraTrack</div>
        <div className="navbar-items">
          <div className="navbar-link" onClick={() => navigate('/assets')}>
            Assets
          </div>
          <div className="navbar-link" onClick={() => navigate('/business-triggers')}>
            Business Triggers
          </div>
          <div className="navbar-link" onClick={() => navigate('/inspections')}>
            Inspections
          </div>
          <div className="navbar-link" onClick={() => navigate('/issues')}>
            Issues
          </div>
          <div className="navbar-link" onClick={() => navigate('/departments')}>
            Departments
          </div>
          <div className="navbar-link" onClick={() => navigate('/asset-categories')}>
            Categories
          </div>
          {canManageUsers(auth?.user?.role) && (
            <div className="navbar-link" onClick={() => navigate('/users')}>
              Users
            </div>
          )}
          <NotificationButton />
          <div className="navbar-link logout" onClick={handleLogout}>
            Logout
          </div>
        </div>
      </nav>

      <div className="platform-content">
        <div className="platform-welcome">
          <h1>InfraTrack</h1>
          <p>Operational asset and field operations management platform.</p>
          
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
              <p>Logged in as: <strong>{auth?.user?.email}</strong> ({getRoleLabel(auth?.user?.role)})</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
