import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import apiClient from '../services/apiClient';
import issueApi from '../services/issueApi';
import inspectionApi from '../services/inspectionApi';
import NotificationButton from '../components/NotificationButton';
import { canRecordIssues } from '../constants/userRoles';
import { getBusinessTriggerTypeLabel } from '../constants/businessTriggerTypes';
import {
  ISSUE_SEVERITIES,
  ISSUE_SEVERITY_OPTIONS,
  getIssueSeverityLabel,
} from '../constants/issueSeverities';
import '../styles/ReferenceDataPage.css';
import '../styles/IssuesPage.css';

function toDateTimeLocalValue(date = new Date()) {
  const pad = (value) => String(value).padStart(2, '0');
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
}

export default function IssuesPage() {
  const navigate = useNavigate();
  const { auth, logout } = useAuth();
  const [issues, setIssues] = useState([]);
  const [inspections, setInspections] = useState([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [formData, setFormData] = useState({
    inspectionId: '',
    description: '',
    severity: ISSUE_SEVERITIES.MEDIUM,
    recordedAt: toDateTimeLocalValue(),
  });

  const canRecord = canRecordIssues(auth?.user?.role);
  const currentUserId = auth?.user?.userId;

  const recordableInspections = useMemo(() => {
    const recordedInspectionIds = new Set(issues.map((issue) => issue.inspectionId));
    return inspections.filter(
      (inspection) =>
        inspection.status === 'COMPLETED'
        && inspection.issueIdentified
        && String(inspection.completedByUserId) === String(currentUserId)
        && !recordedInspectionIds.has(inspection.id)
    );
  }, [inspections, issues, currentUserId]);

  const selectedInspection = useMemo(
    () => inspections.find((inspection) => String(inspection.id) === String(formData.inspectionId)),
    [inspections, formData.inspectionId]
  );

  useEffect(() => {
    if (!auth) {
      navigate('/login');
      return;
    }
    apiClient.setToken(auth.token);
    loadPageData();
  }, [auth, navigate]);

  const loadPageData = async () => {
    try {
      setLoading(true);
      setError(null);
      const [issueData, inspectionData] = await Promise.all([
        issueApi.list(),
        inspectionApi.list(),
      ]);
      setIssues(issueData);
      setInspections(inspectionData);
    } catch (err) {
      setError(`Failed to load issues: ${err.message}`);
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!canRecord) return;

    try {
      setSubmitting(true);
      setError(null);
      setSuccess(null);
      await issueApi.record({
        inspectionId: Number(formData.inspectionId),
        description: formData.description,
        severity: formData.severity,
        recordedAt: `${formData.recordedAt}:00`,
      });
      setSuccess('Issue recorded successfully.');
      setFormData({
        inspectionId: '',
        description: '',
        severity: ISSUE_SEVERITIES.MEDIUM,
        recordedAt: toDateTimeLocalValue(),
      });
      await loadPageData();
    } catch (err) {
      if (err.status === 403) {
        setError('You are not allowed to record this issue.');
      } else {
        setError(`Failed to record issue: ${err.message}`);
      }
    } finally {
      setSubmitting(false);
    }
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  if (loading) {
    return <div className="loading">Loading issues...</div>;
  }

  return (
    <div className="reference-data-page issues-page">
      <header
        className="reference-header"
        style={{
          background: 'linear-gradient(135deg, #1a472a 0%, #2d6b4d 100%)',
          color: 'white',
        }}
      >
        <button type="button" className="back-btn" onClick={() => navigate('/')}>
          ← Back
        </button>
        <h1>Issues</h1>
        <div className="user-header-actions">
          <NotificationButton />
          <button type="button" className="logout-btn" onClick={handleLogout}>
            Logout
          </button>
        </div>
      </header>

      <main className="reference-content issues-content">
        {error && <div className="error-message">{error}</div>}
        {success && <div className="success-message">{success}</div>}

        {canRecord ? (
          <section className="issue-form-section">
            <h2>Record Issue</h2>
            <form className="issue-form" onSubmit={handleSubmit}>
              <div className="form-row">
                <label htmlFor="inspectionId">Completed Inspection</label>
                <select
                  id="inspectionId"
                  name="inspectionId"
                  value={formData.inspectionId}
                  onChange={handleChange}
                  required
                  disabled={submitting || recordableInspections.length === 0}
                >
                  <option value="">Select inspection</option>
                  {recordableInspections.map((inspection) => (
                    <option key={inspection.id} value={inspection.id}>
                      #{inspection.id} — {inspection.assetName} ({getBusinessTriggerTypeLabel(inspection.businessTriggerType)})
                    </option>
                  ))}
                </select>
              </div>

              {selectedInspection && (
                <div className="linked-inspection-info">
                  <strong>Asset:</strong> {selectedInspection.assetName}
                  <br />
                  <strong>Inspection observations:</strong> {selectedInspection.observations}
                  <br />
                  <strong>Completed:</strong>{' '}
                  {selectedInspection.completedAt
                    ? new Date(selectedInspection.completedAt).toLocaleString()
                    : '-'}
                </div>
              )}

              <div className="form-row">
                <label htmlFor="description">Description</label>
                <textarea
                  id="description"
                  name="description"
                  value={formData.description}
                  onChange={handleChange}
                  required
                  disabled={submitting}
                  rows={3}
                />
              </div>

              <div className="form-row">
                <label htmlFor="severity">Severity</label>
                <select
                  id="severity"
                  name="severity"
                  value={formData.severity}
                  onChange={handleChange}
                  required
                  disabled={submitting}
                >
                  {ISSUE_SEVERITY_OPTIONS.map((option) => (
                    <option key={option.value} value={option.value}>
                      {option.label}
                    </option>
                  ))}
                </select>
              </div>

              <div className="form-row">
                <label htmlFor="recordedAt">Recorded Date & Time</label>
                <input
                  id="recordedAt"
                  name="recordedAt"
                  type="datetime-local"
                  value={formData.recordedAt}
                  onChange={handleChange}
                  required
                  disabled={submitting}
                />
              </div>

              <button
                type="submit"
                className="btn-primary"
                disabled={submitting || recordableInspections.length === 0}
              >
                {submitting ? 'Recording...' : 'Record Issue'}
              </button>
            </form>
            {recordableInspections.length === 0 && (
              <p className="read-only-note">
                No completed inspections with identified issues are available for you to record.
              </p>
            )}
          </section>
        ) : (
          <p className="read-only-note">
            Issue recording is available to Field Employees and Contractors who completed the inspection.
          </p>
        )}

        <section className="issue-list-section">
          <h2>Recorded Issues</h2>
          {issues.length === 0 ? (
            <p className="no-items">No issues recorded yet.</p>
          ) : (
            <table className="reference-table issues-table">
              <thead>
                <tr>
                  <th>Asset</th>
                  <th>Inspection</th>
                  <th>Description</th>
                  <th>Severity</th>
                  <th>Recorded</th>
                </tr>
              </thead>
              <tbody>
                {issues.map((issue) => (
                  <tr key={issue.id}>
                    <td>{issue.assetName}</td>
                    <td>#{issue.inspectionId}</td>
                    <td>{issue.description}</td>
                    <td>{getIssueSeverityLabel(issue.severity)}</td>
                    <td>
                      {issue.recordedAt
                        ? new Date(issue.recordedAt).toLocaleString()
                        : '-'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </section>
      </main>
    </div>
  );
}
