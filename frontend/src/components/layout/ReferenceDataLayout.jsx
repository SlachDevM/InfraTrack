import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { ROUTES } from '../../constants/routes';
import { COMMON_LABELS } from '../../constants/uiLabels';
import NotificationButton from '../NotificationButton';
import '../../styles/ReferenceDataPage.css';

export const REFERENCE_HEADER_STYLE_GREEN = {
  background: 'linear-gradient(135deg, #1a472a 0%, #2d6b4d 100%)',
  color: 'white',
};

export const REFERENCE_HEADER_STYLE_BLUE = {
  background: 'linear-gradient(135deg, #1a3a5c 0%, #2d5a8a 100%)',
  color: 'white',
};

export default function ReferenceDataLayout({
  title,
  children,
  showBackButton = true,
  backLabel = COMMON_LABELS.BACK,
  onBack,
  pageClassName = '',
  headerStyle = REFERENCE_HEADER_STYLE_GREEN,
  headerActions = null,
}) {
  const navigate = useNavigate();
  const { logout } = useAuth();

  const handleBack = onBack ?? (() => navigate(ROUTES.HOME));

  const handleLogout = () => {
    logout();
    navigate(ROUTES.LOGIN);
  };

  const pageClasses = ['reference-data-page', pageClassName].filter(Boolean).join(' ');

  return (
    <div className={pageClasses}>
      <header className="reference-header" style={headerStyle}>
        {showBackButton && (
          <button type="button" className="back-btn" onClick={handleBack} aria-label={backLabel}>
            {backLabel}
          </button>
        )}
        <h1>{title}</h1>
        <div className="user-header-actions page-actions">
          <NotificationButton />
          {headerActions}
          <button type="button" className="logout-btn" onClick={handleLogout} aria-label="Log out">
            Logout
          </button>
        </div>
      </header>

      <main className="reference-content">{children}</main>
    </div>
  );
}
