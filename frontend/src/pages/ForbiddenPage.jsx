import { useNavigate } from 'react-router-dom';
import { ROUTES } from '../constants/routes';
import { COMMON_MESSAGES } from '../constants/messages';
import '../styles/ReferenceDataPage.css';

export default function ForbiddenPage() {
  const navigate = useNavigate();

  return (
    <div className="reference-data-page">
      <main className="reference-content">
        <h1>Access Denied</h1>
        <p>{COMMON_MESSAGES.UNAUTHORIZED_PAGE}</p>
        <button type="button" className="action-btn edit-btn" onClick={() => navigate(ROUTES.HOME)}>
          Return to Dashboard
        </button>
      </main>
    </div>
  );
}
