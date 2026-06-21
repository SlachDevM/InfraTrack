import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import NotificationButton from '../components/NotificationButton';
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
        <div className="navbar-brand">Business Platform Template</div>
        <div className="navbar-items">
          <div className="navbar-link" onClick={() => navigate('/users')}>
            Users
          </div>
          <NotificationButton />
          <div className="navbar-link logout" onClick={handleLogout}>
            Logout
          </div>
        </div>
      </nav>

      <div className="platform-content">
        <div className="platform-welcome">
          <h1>Business Platform Template</h1>
          <p>Production-ready foundation for business applications.</p>
          
          <div className="platform-info">
            <h2>Getting Started</h2>
            <p>
              This template provides a complete infrastructure for authentication, user management, 
              notifications, and deployment.
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
                Add your business domain by creating:
              </p>
              <ul>
                <li>Backend entity models in <code>backend/src/main/java/com/mrrg/backend/model/</code></li>
                <li>Services in <code>backend/src/main/java/com/mrrg/backend/service/</code></li>
                <li>Controllers in <code>backend/src/main/java/com/mrrg/backend/controller/</code></li>
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
                <li>Project Philosophy</li>
                <li>Software Architecture</li>
                <li>Installation Guide</li>
                <li>Security Configuration</li>
                <li>Firebase Setup</li>
              </ul>
            </div>

            <div className="user-info">
              <p>Logged in as: <strong>{auth?.user?.email}</strong> ({auth?.user?.role})</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
