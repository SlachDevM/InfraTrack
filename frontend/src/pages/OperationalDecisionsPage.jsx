import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import apiClient from '../services/apiClient';
import operationalDecisionApi from '../services/operationalDecisionApi';
import issueApi from '../services/issueApi';
import NotificationButton from '../components/NotificationButton';
import PaginationControls from '../components/PaginationControls';
import { canMakeOperationalDecisions } from '../constants/userRoles';
import { getApiErrorMessage, isForbidden } from '../utils/apiError';
import {
  DEFAULT_PAGE,
  MAX_PAGE_SIZE,
  getPageNumber,
  getTotalPages,
  unwrapPageContent,
} from '../utils/pagination';
import { getIssueSeverityLabel } from '../constants/issueSeverities';
import {
  OPERATIONAL_DECISION_OUTCOMES,
  OPERATIONAL_DECISION_OUTCOME_OPTIONS,
  getOperationalDecisionOutcomeLabel,
} from '../constants/operationalDecisionOutcomes';
import '../styles/ReferenceDataPage.css';
import '../styles/OperationalDecisionsPage.css';

function toDateTimeLocalValue(date = new Date()) {
  const pad = (value) => String(value).padStart(2, '0');
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
}

export default function OperationalDecisionsPage() {
  const navigate = useNavigate();
  const { auth, logout } = useAuth();
  const [decisions, setDecisions] = useState([]);
  const [decisionsPage, setDecisionsPage] = useState(DEFAULT_PAGE);
  const [decisionsTotalPages, setDecisionsTotalPages] = useState(0);
  const [listLoading, setListLoading] = useState(false);
  const [issues, setIssues] = useState([]);
  const [allDecisions, setAllDecisions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [formData, setFormData] = useState({
    issueId: '',
    outcome: OPERATIONAL_DECISION_OUTCOMES.CONTINUE_MONITORING,
    rationale: '',
    decidedAt: toDateTimeLocalValue(),
  });

  const canDecide = canMakeOperationalDecisions(auth?.user?.role);

  const issuesRequiringDecision = useMemo(() => {
    const decidedIssueIds = new Set(allDecisions.map((decision) => decision.issueId));
    return issues.filter((issue) => !decidedIssueIds.has(issue.id));
  }, [issues, allDecisions]);

  const selectedIssue = useMemo(
    () => issues.find((issue) => String(issue.id) === String(formData.issueId)),
    [issues, formData.issueId]
  );

  useEffect(() => {
    if (!auth) {
      navigate('/login');
      return;
    }
    apiClient.setToken(auth.token);
    loadPageData();
  }, [auth, navigate]);

  const loadDecisions = async (page = decisionsPage) => {
    try {
      setListLoading(true);
      setError(null);
      const decisionPage = await operationalDecisionApi.list(page);
      setDecisions(unwrapPageContent(decisionPage));
      setDecisionsPage(getPageNumber(decisionPage, page));
      setDecisionsTotalPages(getTotalPages(decisionPage));
    } catch (err) {
      setError(getApiErrorMessage(err, 'Failed to load operational decisions.'));
    } finally {
      setListLoading(false);
    }
  };

  const loadPageData = async (page = decisionsPage) => {
    try {
      setLoading(true);
      setError(null);
      const [decisionPage, issuePage, allDecisionsPage] = await Promise.all([
        operationalDecisionApi.list(page),
        issueApi.list(0, MAX_PAGE_SIZE),
        canDecide ? operationalDecisionApi.list(0, MAX_PAGE_SIZE) : Promise.resolve(null),
      ]);
      setDecisions(unwrapPageContent(decisionPage));
      setDecisionsPage(getPageNumber(decisionPage, page));
      setDecisionsTotalPages(getTotalPages(decisionPage));
      setIssues(unwrapPageContent(issuePage));
      setAllDecisions(allDecisionsPage ? unwrapPageContent(allDecisionsPage) : []);
    } catch (err) {
      setError(getApiErrorMessage(err, 'Failed to load operational decisions.'));
    } finally {
      setLoading(false);
      setListLoading(false);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!canDecide) return;

    try {
      setSubmitting(true);
      setError(null);
      setSuccess(null);
      await operationalDecisionApi.create({
        issueId: Number(formData.issueId),
        outcome: formData.outcome,
        rationale: formData.rationale,
        decidedAt: `${formData.decidedAt}:00`,
      });
      setSuccess('Operational decision recorded successfully.');
      setFormData({
        issueId: '',
        outcome: OPERATIONAL_DECISION_OUTCOMES.CONTINUE_MONITORING,
        rationale: '',
        decidedAt: toDateTimeLocalValue(),
      });
      await loadPageData(decisionsPage);
    } catch (err) {
      if (isForbidden(err)) {
        setError('You do not have permission to make operational decisions.');
      } else {
        setError(getApiErrorMessage(err, 'Failed to make operational decision.'));
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
    return <div className="loading">Loading operational decisions...</div>;
  }

  return (
    <div className="reference-data-page operational-decisions-page">
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
        <h1>Operational Decisions</h1>
        <div className="user-header-actions">
          <NotificationButton />
          <button type="button" className="logout-btn" onClick={handleLogout}>
            Logout
          </button>
        </div>
      </header>

      <main className="reference-content operational-decisions-content">
        {error && <div className="error-message">{error}</div>}
        {success && <div className="success-message">{success}</div>}

        {canDecide ? (
          <section className="decision-form-section">
            <h2>Make Operational Decision</h2>
            <form className="decision-form" onSubmit={handleSubmit}>
              <div className="form-row">
                <label htmlFor="issueId">Issue</label>
                <select
                  id="issueId"
                  name="issueId"
                  value={formData.issueId}
                  onChange={handleChange}
                  required
                  disabled={submitting || issuesRequiringDecision.length === 0}
                >
                  <option value="">Select issue</option>
                  {issuesRequiringDecision.map((issue) => (
                    <option key={issue.id} value={issue.id}>
                      #{issue.id} — {issue.assetName} ({getIssueSeverityLabel(issue.severity)})
                    </option>
                  ))}
                </select>
              </div>

              {selectedIssue && (
                <div className="linked-issue-info">
                  <strong>Asset:</strong> {selectedIssue.assetName}
                  <br />
                  <strong>Issue:</strong> {selectedIssue.description}
                  <br />
                  <strong>Recorded:</strong>{' '}
                  {selectedIssue.recordedAt
                    ? new Date(selectedIssue.recordedAt).toLocaleString()
                    : '-'}
                </div>
              )}

              <div className="form-row">
                <label htmlFor="outcome">Outcome</label>
                <select
                  id="outcome"
                  name="outcome"
                  value={formData.outcome}
                  onChange={handleChange}
                  required
                  disabled={submitting}
                >
                  {OPERATIONAL_DECISION_OUTCOME_OPTIONS.map((option) => (
                    <option key={option.value} value={option.value}>
                      {option.label}
                    </option>
                  ))}
                </select>
              </div>

              <div className="form-row">
                <label htmlFor="rationale">Rationale</label>
                <textarea
                  id="rationale"
                  name="rationale"
                  value={formData.rationale}
                  onChange={handleChange}
                  required
                  disabled={submitting}
                  rows={3}
                />
              </div>

              <div className="form-row">
                <label htmlFor="decidedAt">Decision Date & Time</label>
                <input
                  id="decidedAt"
                  name="decidedAt"
                  type="datetime-local"
                  value={formData.decidedAt}
                  onChange={handleChange}
                  required
                  disabled={submitting}
                />
              </div>

              <button
                type="submit"
                className="btn-primary"
                disabled={submitting || issuesRequiringDecision.length === 0}
              >
                {submitting ? 'Recording...' : 'Make Operational Decision'}
              </button>
            </form>
            {issuesRequiringDecision.length === 0 && (
              <p className="read-only-note">No issues are currently awaiting an operational decision.</p>
            )}
          </section>
        ) : (
          <p className="read-only-note">
            Operational decisions are available to Managers.
          </p>
        )}

        <section className="decision-list-section">
          <h2>Operational Decisions</h2>
          {decisions.length === 0 ? (
            <p className="no-items">No operational decisions yet.</p>
          ) : (
            <table className="reference-table decisions-table">
              <thead>
                <tr>
                  <th>Asset</th>
                  <th>Issue</th>
                  <th>Outcome</th>
                  <th>Rationale</th>
                  <th>Decided</th>
                </tr>
              </thead>
              <tbody>
                {decisions.map((decision) => (
                  <tr key={decision.id}>
                    <td>{decision.assetName}</td>
                    <td>#{decision.issueId}</td>
                    <td>{getOperationalDecisionOutcomeLabel(decision.outcome)}</td>
                    <td>{decision.rationale}</td>
                    <td>
                      {decision.decidedAt
                        ? new Date(decision.decidedAt).toLocaleString()
                        : '-'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </section>
        <PaginationControls
          page={decisionsPage}
          totalPages={decisionsTotalPages}
          loading={listLoading}
          onPrevious={() => loadDecisions(decisionsPage - 1)}
          onNext={() => loadDecisions(decisionsPage + 1)}
        />
      </main>
    </div>
  );
}
