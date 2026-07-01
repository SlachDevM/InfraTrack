import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import apiClient from '../services/apiClient';
import preventiveSchedulerApi from '../services/preventiveSchedulerApi';
import ReferenceDataLayout from '../components/layout/ReferenceDataLayout';
import PaginationControls from '../components/PaginationControls';
import {
  canRunPreventiveScheduler,
  canViewPreventiveScheduler,
} from '../constants/userRoles';
import { ROUTES } from '../constants/routes';
import {
  getSchedulerRunStatusLabel,
  getSchedulerTriggeredByLabel,
} from '../constants/schedulerRunStatuses';
import { getApiErrorMessage, isForbidden } from '../utils/apiError';
import {
  DEFAULT_PAGE,
  getPageNumber,
  getTotalPages,
  unwrapPageContent,
} from '../utils/pagination';

function formatTimestamp(timestamp) {
  if (!timestamp) {
    return '-';
  }
  return new Date(timestamp).toLocaleString();
}

export default function PreventiveSchedulerPage() {
  const navigate = useNavigate();
  const { auth } = useAuth();
  const [schedulerEnabled, setSchedulerEnabled] = useState(false);
  const [runs, setRuns] = useState([]);
  const [runsPage, setRunsPage] = useState(DEFAULT_PAGE);
  const [runsTotalPages, setRunsTotalPages] = useState(0);
  const [selectedRun, setSelectedRun] = useState(null);
  const [loading, setLoading] = useState(true);
  const [listLoading, setListLoading] = useState(false);
  const [running, setRunning] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [lastRunResult, setLastRunResult] = useState(null);

  const canView = canViewPreventiveScheduler(auth?.user?.role);
  const canRun = canRunPreventiveScheduler(auth?.user?.role);

  useEffect(() => {
    if (!auth) {
      navigate(ROUTES.LOGIN);
      return;
    }
    if (!canView) {
      navigate(ROUTES.HOME);
      return;
    }
    apiClient.setToken(auth.token);
    loadPageData();
  }, [auth, canView, navigate]);

  const loadRuns = async (page = runsPage) => {
    const runPage = await preventiveSchedulerApi.listRuns(page);
    setRuns(unwrapPageContent(runPage));
    setRunsPage(getPageNumber(runPage, page));
    setRunsTotalPages(getTotalPages(runPage));
  };

  const loadPageData = async (page = DEFAULT_PAGE) => {
    try {
      setLoading(true);
      setError(null);
      const [status, runPage] = await Promise.all([
        preventiveSchedulerApi.getStatus(),
        preventiveSchedulerApi.listRuns(page),
      ]);
      setSchedulerEnabled(Boolean(status?.enabled));
      setRuns(unwrapPageContent(runPage));
      setRunsPage(getPageNumber(runPage, page));
      setRunsTotalPages(getTotalPages(runPage));
    } catch (err) {
      setError(getApiErrorMessage(err, 'Failed to load preventive scheduler.'));
    } finally {
      setLoading(false);
    }
  };

  const handleRun = async () => {
    if (!canRun) return;

    try {
      setRunning(true);
      setError(null);
      setSuccess(null);
      setLastRunResult(null);
      const result = await preventiveSchedulerApi.run();
      setLastRunResult(result);
      setSuccess(
        `Scheduler run complete: ${result.candidatesCreatedCount} created, `
        + `${result.candidatesSkippedDuplicateCount} skipped, `
        + `${result.plansNotEligibleCount} not eligible.`
      );
      await loadRuns(DEFAULT_PAGE);
      setSelectedRun(null);
    } catch (err) {
      if (isForbidden(err)) {
        setError('You do not have permission to run the preventive scheduler.');
      } else {
        setError(getApiErrorMessage(err, 'Failed to run preventive scheduler.'));
      }
    } finally {
      setRunning(false);
    }
  };

  const handleViewRun = async (runId) => {
    try {
      setListLoading(true);
      setError(null);
      const run = await preventiveSchedulerApi.getRun(runId);
      setSelectedRun(run);
    } catch (err) {
      setError(getApiErrorMessage(err, 'Failed to load scheduler run detail.'));
    } finally {
      setListLoading(false);
    }
  };

  if (loading) {
    return <div className="loading">Loading preventive scheduler...</div>;
  }

  return (
    <ReferenceDataLayout title="Preventive Scheduler">
        {error && <div className="error-message">{error}</div>}
        {success && <div className="success-message">{success}</div>}

        <section className="reference-form-section">
        <div className="section-header">
          <h2>Scheduler Status</h2>
          {canRun && (
            <button
              type="button"
              className="btn-primary"
              onClick={handleRun}
              disabled={running}
            >
              {running ? 'Running...' : 'Run Scheduler'}
            </button>
          )}
        </div>
        <p>
          Scheduled execution:
          {' '}
          <strong>{schedulerEnabled ? 'Enabled' : 'Disabled'}</strong>
        </p>
        <p className="help-text">
          The scheduler generates preventive execution candidates only.
          Managers still review and approve candidates manually.
        </p>
        {lastRunResult && (
          <dl className="detail-list">
            <dt>Last Run Status</dt>
            <dd>{getSchedulerRunStatusLabel(lastRunResult.status)}</dd>
            <dt>Plans Evaluated</dt>
            <dd>{lastRunResult.plansEvaluatedCount}</dd>
            <dt>Candidates Created</dt>
            <dd>{lastRunResult.candidatesCreatedCount}</dd>
            <dt>Skipped Duplicates</dt>
            <dd>{lastRunResult.candidatesSkippedDuplicateCount}</dd>
            <dt>Not Eligible</dt>
            <dd>{lastRunResult.plansNotEligibleCount}</dd>
            <dt>Duration</dt>
            <dd>
              {lastRunResult.durationMs}
              {' '}
              ms
            </dd>
          </dl>
        )}
      </section>

      <section className="reference-form-section">
        <h2>Run History</h2>
        {listLoading ? (
          <p className="loading-state-inline" role="status">Loading runs...</p>
        ) : (
          <div className="table-scroll">
          <table className="reference-table">
            <thead>
              <tr>
                <th>Started</th>
                <th>Status</th>
                <th>Triggered By</th>
                <th>Created</th>
                <th>Skipped</th>
                <th>Not Eligible</th>
                <th>Duration</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {runs.length === 0 ? (
                <tr>
                  <td colSpan={8} className="empty-state">No scheduler runs found.</td>
                </tr>
              ) : (
                runs.map((run) => (
                  <tr key={run.id}>
                    <td>{formatTimestamp(run.startedAt)}</td>
                    <td>{getSchedulerRunStatusLabel(run.status)}</td>
                    <td>{getSchedulerTriggeredByLabel(run.triggeredBy)}</td>
                    <td>{run.candidatesCreatedCount}</td>
                    <td>{run.candidatesSkippedDuplicateCount}</td>
                    <td>{run.plansNotEligibleCount}</td>
                    <td>
                      {run.durationMs}
                      {' '}
                      ms
                    </td>
                    <td>
                      <button
                        type="button"
                        className="btn-link"
                        onClick={() => handleViewRun(run.id)}
                      >
                        View
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
          </div>
        )}
      </section>

      <PaginationControls
        page={runsPage}
        totalPages={runsTotalPages}
        loading={listLoading}
        onPrevious={() => {
          setListLoading(true);
          loadRuns(runsPage - 1).finally(() => setListLoading(false));
        }}
        onNext={() => {
          setListLoading(true);
          loadRuns(runsPage + 1).finally(() => setListLoading(false));
        }}
      />

      {selectedRun && (
        <section className="reference-form-section">
          <div className="section-header">
            <h2>Run Detail</h2>
            <button
              type="button"
              className="btn-secondary"
              onClick={() => setSelectedRun(null)}
            >
              Close
            </button>
          </div>
          <dl className="detail-list">
            <dt>Status</dt>
            <dd>{getSchedulerRunStatusLabel(selectedRun.status)}</dd>
            <dt>Triggered By</dt>
            <dd>{getSchedulerTriggeredByLabel(selectedRun.triggeredBy)}</dd>
            <dt>Started At</dt>
            <dd>{formatTimestamp(selectedRun.startedAt)}</dd>
            <dt>Finished At</dt>
            <dd>{formatTimestamp(selectedRun.finishedAt)}</dd>
            <dt>Duration</dt>
            <dd>
              {selectedRun.durationMs}
              {' '}
              ms
            </dd>
            <dt>Plans Evaluated</dt>
            <dd>{selectedRun.plansEvaluatedCount}</dd>
            <dt>Candidates Created</dt>
            <dd>{selectedRun.candidatesCreatedCount}</dd>
            <dt>Skipped Duplicates</dt>
            <dd>{selectedRun.candidatesSkippedDuplicateCount}</dd>
            <dt>Not Eligible</dt>
            <dd>{selectedRun.plansNotEligibleCount}</dd>
            {selectedRun.errorMessage && (
              <>
                <dt>Error</dt>
                <dd>{selectedRun.errorMessage}</dd>
              </>
            )}
          </dl>
        </section>
      )}
    </ReferenceDataLayout>
  );
}
