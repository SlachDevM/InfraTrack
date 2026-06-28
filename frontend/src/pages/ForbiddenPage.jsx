import { useNavigate } from 'react-router-dom';
import '../styles/ReferenceDataPage.css';

export default function ForbiddenPage() {
  const navigate = useNavigate();

  return (
    <div className="reference-data-page">
      <main className="reference-content">
        <h1>Access Denied</h1>
        <p>You do not have permission to view this page.</p>
        <button type="button" className="action-btn edit-btn" onClick={() => navigate('/')}>
          Return to Dashboard
        </button>
      </main>
    </div>
  );
}
