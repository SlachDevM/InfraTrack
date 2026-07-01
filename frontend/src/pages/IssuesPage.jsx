import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import apiClient from '../services/apiClient';
import issueApi from '../services/issueApi';
import inspectionApi from '../services/inspectionApi';
import NotificationButton from '../components/NotificationButton';
import PaginationControls from '../components/PaginationControls';
import ExportCsvButton from '../components/ExportCsvButton';
import { canMakeOperationalDecisions, canRecordIssues, canExportReporting } from '../constants/userRoles';
import { getApiErrorMessage, isForbidden } from '../utils/apiError';
import {
  DEFAULT_PAGE,
  MAX_PAGE_SIZE,
  getPageNumber,
  getTotalPages,
  unwrapPageContent,
} from '../utils/pagination';
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

function appendOptionalCapaFields(payload, formData) {
  if (formData.rootCause.trim()) {
    payload.rootCause = formData.rootCause.trim();
  }
  if (formData.correctiveAction.trim()) {
    payload.correctiveAction = formData.correctiveAction.trim();
  }
  if (formData.preventiveAction.trim()) {
    payload.preventiveAction = formData.preventiveAction.trim();
  }
  if (formData.lessonsLearned.trim()) {
    payload.lessonsLearned = formData.lessonsLearned.trim();
  }
  return payload;
}

function displayCapaValue(value) {
  return value || '-';
}

const EMPTY_CAPA_FIELDS = {
  rootCause: '',
  correctiveAction: '',
  preventiveAction: '',
  lessonsLearned: '',
};

export default function IssuesPage() {
  const navigate = useNavigate();
  const { auth, logout } = useAuth();
  const [issues, setIssues] = useState([]);
  const [issuesPage, setIssuesPage] = useState(DEFAULT_PAGE);
  const [issuesTotalPages, setIssuesTotalPages] = useState(0);
  const [listLoading, setListLoading] = useState(false);
  const [eligibleInspections, setEligibleInspections] = useState([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [updatingCapa, setUpdatingCapa] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [formData, setFormData] = useState({
    inspectionId: '',
    description: '',
    severity: ISSUE_SEVERITIES.MEDIUM,
    recordedAt: toDateTimeLocalValue(),
    ...EMPTY_CAPA_FIELDS,
  });
  const [capaEditFormData, setCapaEditFormData] = useState({
    issueId: '',
    ...EMPTY_CAPA_FIELDS,
  });

  const canRecord = canRecordIssues(auth?.user?.role);
  const canEditCapa = canMakeOperationalDecisions(auth?.user?.role);
  const canExport = canExportReporting(auth?.user?.role);

  const selectedInspection = useMemo(
    () => eligibleInspections.find(
      (inspection) => String(inspection.id) === String(formData.inspectionId)
    ),
    [eligibleInspections, formData.inspectionId]
  );

  const selectedCapaIssue = useMemo(
    () => issues.find((issue) => String(issue.id) === String(capaEditFormData.issueId)),
    [issues, capaEditFormData.issueId]
  );

  useEffect(() => {
    if (!auth) {
      navigate('/login');
      return;
    }
    apiClient.setToken(auth.token);
    loadPageData();
  }, [auth, navigate]);

  const loadIssues = async (page = issuesPage) => {
    try {
      setListLoading(true);
      setError(null);
      const issuePage = await issueApi.list(page);
      setIssues(unwrapPageContent(issuePage));
      setIssuesPage(getPageNumber(issuePage, page));
      setIssuesTotalPages(getTotalPages(issuePage));
    } catch (err) {
      setError(getApiErrorMessage(err, 'Failed to load issues.'));
    } finally {
      setListLoading(false);
    }
  };

  const loadPageData = async (page = issuesPage) => {
    try {
      setLoading(true);
      setError(null);
      const issuePage = await issueApi.list(page);
      setIssues(unwrapPageContent(issuePage));
      setIssuesPage(getPageNumber(issuePage, page));
      setIssuesTotalPages(getTotalPages(issuePage));

      if (canRecord) {
        const inspectionPage = await inspectionApi.listEligibleForIssueRecording(0, MAX_PAGE_SIZE);
        setEligibleInspections(unwrapPageContent(inspectionPage));
      } else {
        setEligibleInspections([]);
      }
    } catch (err) {
      setError(getApiErrorMessage(err, 'Failed to load issues.'));
    } finally {
      setLoading(false);
      setListLoading(false);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleCapaEditChange = (e) => {
    const { name, value } = e.target;
    if (name === 'issueId') {
      const issue = issues.find((entry) => String(entry.id) === String(value));
      setCapaEditFormData({
        issueId: value,
        rootCause: issue?.rootCause || '',
        correctiveAction: issue?.correctiveAction || '',
        preventiveAction: issue?.preventiveAction || '',
        lessonsLearned: issue?.lessonsLearned || '',
      });
      return;
    }
    setCapaEditFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!canRecord) return;

    try {
      setSubmitting(true);
      setError(null);
      setSuccess(null);
      const payload = appendOptionalCapaFields({
        inspectionId: Number(formData.inspectionId),
        description: formData.description,
        severity: formData.severity,
        recordedAt: `${formData.recordedAt}:00`,
      }, formData);
      await issueApi.record(payload);
      setSuccess('Issue recorded successfully.');
      setFormData({
        inspectionId: '',
        description: '',
        severity: ISSUE_SEVERITIES.MEDIUM,
        recordedAt: toDateTimeLocalValue(),
        ...EMPTY_CAPA_FIELDS,
      });
      await loadPageData(issuesPage);
    } catch (err) {
      if (isForbidden(err)) {
        setError('You are not allowed to record this issue.');
      } else {
        setError(getApiErrorMessage(err, 'Failed to record issue.'));
      }
    } finally {
      setSubmitting(false);
    }
  };

  const handleCapaEditSubmit = async (e) => {
    e.preventDefault();
    if (!canEditCapa || !capaEditFormData.issueId) return;

    try {
      setUpdatingCapa(true);
      setError(null);
      setSuccess(null);
      await issueApi.updateCapa(Number(capaEditFormData.issueId), {
        rootCause: capaEditFormData.rootCause.trim() || null,
        correctiveAction: capaEditFormData.correctiveAction.trim() || null,
        preventiveAction: capaEditFormData.preventiveAction.trim() || null,
        lessonsLearned: capaEditFormData.lessonsLearned.trim() || null,
      });
      setSuccess('Issue CAPA metadata updated successfully.');
      setCapaEditFormData({
        issueId: '',
        ...EMPTY_CAPA_FIELDS,
      });
      await loadPageData(issuesPage);
    } catch (err) {
      if (isForbidden(err)) {
        setError('You do not have permission to update CAPA metadata for this issue.');
      } else {
        setError(getApiErrorMessage(err, 'Failed to update issue CAPA metadata.'));
      }
    } finally {
      setUpdatingCapa(false);
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
          {canExport && <ExportCsvButton exportType="issues" onError={setError} />}
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
                  disabled={submitting || eligibleInspections.length === 0}
                >
                  <option value="">Select inspection</option>
                  {eligibleInspections.map((inspection) => (
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
                <label htmlFor="rootCause">Root Cause</label>
                <textarea
                  id="rootCause"
                  name="rootCause"
                  value={formData.rootCause}
                  onChange={handleChange}
                  disabled={submitting}
                  rows={2}
                />
              </div>

              <div className="form-row">
                <label htmlFor="correctiveAction">Corrective Action</label>
                <textarea
                  id="correctiveAction"
                  name="correctiveAction"
                  value={formData.correctiveAction}
                  onChange={handleChange}
                  disabled={submitting}
                  rows={2}
                />
              </div>

              <div className="form-row">
                <label htmlFor="preventiveAction">Preventive Action</label>
                <textarea
                  id="preventiveAction"
                  name="preventiveAction"
                  value={formData.preventiveAction}
                  onChange={handleChange}
                  disabled={submitting}
                  rows={2}
                />
              </div>

              <div className="form-row">
                <label htmlFor="lessonsLearned">Lessons Learned</label>
                <textarea
                  id="lessonsLearned"
                  name="lessonsLearned"
                  value={formData.lessonsLearned}
                  onChange={handleChange}
                  disabled={submitting}
                  rows={2}
                />
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
                disabled={submitting || eligibleInspections.length === 0}
              >
                {submitting ? 'Recording...' : 'Record Issue'}
              </button>
            </form>
            {eligibleInspections.length === 0 && (
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

        {canEditCapa ? (
          <section className="issue-form-section">
            <h2>Update Issue CAPA</h2>
            <form className="issue-form" onSubmit={handleCapaEditSubmit}>
              <div className="form-row">
                <label htmlFor="issueId">Issue</label>
                <select
                  id="issueId"
                  name="issueId"
                  value={capaEditFormData.issueId}
                  onChange={handleCapaEditChange}
                  required
                  disabled={updatingCapa || issues.length === 0}
                >
                  <option value="">Select issue</option>
                  {issues.map((issue) => (
                    <option key={issue.id} value={issue.id}>
                      #{issue.id} — {issue.assetName} — {issue.description}
                    </option>
                  ))}
                </select>
              </div>

              {selectedCapaIssue && (
                <div className="linked-inspection-info">
                  <strong>Asset:</strong> {selectedCapaIssue.assetName}
                  <br />
                  <strong>Description:</strong> {selectedCapaIssue.description}
                </div>
              )}

              <div className="form-row">
                <label htmlFor="editRootCause">Root Cause</label>
                <textarea
                  id="editRootCause"
                  name="rootCause"
                  value={capaEditFormData.rootCause}
                  onChange={handleCapaEditChange}
                  disabled={updatingCapa}
                  rows={2}
                />
              </div>

              <div className="form-row">
                <label htmlFor="editCorrectiveAction">Corrective Action</label>
                <textarea
                  id="editCorrectiveAction"
                  name="correctiveAction"
                  value={capaEditFormData.correctiveAction}
                  onChange={handleCapaEditChange}
                  disabled={updatingCapa}
                  rows={2}
                />
              </div>

              <div className="form-row">
                <label htmlFor="editPreventiveAction">Preventive Action</label>
                <textarea
                  id="editPreventiveAction"
                  name="preventiveAction"
                  value={capaEditFormData.preventiveAction}
                  onChange={handleCapaEditChange}
                  disabled={updatingCapa}
                  rows={2}
                />
              </div>

              <div className="form-row">
                <label htmlFor="editLessonsLearned">Lessons Learned</label>
                <textarea
                  id="editLessonsLearned"
                  name="lessonsLearned"
                  value={capaEditFormData.lessonsLearned}
                  onChange={handleCapaEditChange}
                  disabled={updatingCapa}
                  rows={2}
                />
              </div>

              <button
                type="submit"
                className="btn-primary"
                disabled={updatingCapa || issues.length === 0}
              >
                {updatingCapa ? 'Updating...' : 'Update CAPA Metadata'}
              </button>
            </form>
          </section>
        ) : null}

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
                  <th>Root Cause</th>
                  <th>Lessons Learned</th>
                  <th>Recorded</th>
                </tr>
              </thead>
              <tbody>
                {issues.map((issue) => (
                  <tr key={issue.id}>
                    <td>{issue.assetName}</td>
                    <td>{issue.inspectionId ? `#${issue.inspectionId}` : '-'}</td>
                    <td>{issue.description}</td>
                    <td>{getIssueSeverityLabel(issue.severity)}</td>
                    <td>{displayCapaValue(issue.rootCause)}</td>
                    <td>{displayCapaValue(issue.lessonsLearned)}</td>
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
        <PaginationControls
          page={issuesPage}
          totalPages={issuesTotalPages}
          loading={listLoading}
          onPrevious={() => loadIssues(issuesPage - 1)}
          onNext={() => loadIssues(issuesPage + 1)}
        />
      </main>
    </div>
  );
}
