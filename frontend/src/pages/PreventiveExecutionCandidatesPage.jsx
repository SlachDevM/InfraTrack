import { useEffect, useMemo, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import apiClient from '../services/apiClient';
import preventiveExecutionCandidateApi from '../services/preventiveExecutionCandidateApi';
import preventiveMaintenancePlanApi from '../services/preventiveMaintenancePlanApi';
import assetApi from '../services/assetApi';
import inspectionApi from '../services/inspectionApi';
import userApi from '../services/userApi';
import ReferenceDataLayout from '../components/layout/ReferenceDataLayout';
import ExportCsvButton from '../components/ExportCsvButton';
import ExportReportingButton from '../components/ExportReportingButton';
import PaginationControls from '../components/PaginationControls';
import {
  canExportReporting,
  canGeneratePreventiveExecutionCandidates,
  canReviewPreventiveExecutionCandidates,
  canViewPreventiveExecutionCandidates,
} from '../constants/userRoles';
import { PREVENTIVE_CANDIDATE_STATUS } from '../constants/statuses';
import { REPORTING_EXPORT_FORMATS, REPORTING_EXPORT_TYPES } from '../constants/reportingExports';
import { ROUTES } from '../constants/routes';
import {
  EXECUTION_CANDIDATE_STATUS_OPTIONS,
  getExecutionCandidateStatusLabel,
} from '../constants/executionCandidateStatuses';
import {
  getDecisionSourceLabel,
  getExecutionReportStatusLabel,
} from '../constants/executionReportStatuses';
import { getPlanTargetActionLabel } from '../constants/planTargetActions';
import { getApiErrorMessage, isForbidden } from '../utils/apiError';
import { filterInspectionAssignees } from '../utils/inspectionAssignees';
import {
  DEFAULT_PAGE,
  MAX_PAGE_SIZE,
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

function dateInputToPlannedAt(value) {
  if (!value) {
    return undefined;
  }
  return new Date(`${value}T00:00:00`).getTime();
}

function parseAssigneeId(value) {
  if (value === '' || value == null) {
    return null;
  }
  const parsed = Number(value);
  if (!Number.isFinite(parsed) || parsed <= 0) {
    return null;
  }
  return parsed;
}

const EMPTY_APPROVE_FORM = {
  assigneeId: '',
  plannedAt: '',
  notes: '',
};

export default function PreventiveExecutionCandidatesPage() {
  const navigate = useNavigate();
  const { auth } = useAuth();
  const [candidates, setCandidates] = useState([]);
  const [assets, setAssets] = useState([]);
  const [plans, setPlans] = useState([]);
  const [candidatesPage, setCandidatesPage] = useState(DEFAULT_PAGE);
  const [candidatesTotalPages, setCandidatesTotalPages] = useState(0);
  const [listLoading, setListLoading] = useState(false);
  const [loading, setLoading] = useState(true);
  const [generating, setGenerating] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [filterStatus, setFilterStatus] = useState('');
  const [filterAssetId, setFilterAssetId] = useState('');
  const [filterPlanId, setFilterPlanId] = useState('');
  const [selectedCandidate, setSelectedCandidate] = useState(null);
  const [selectedReport, setSelectedReport] = useState(null);
  const [detailTab, setDetailTab] = useState('candidate');
  const [detailLoading, setDetailLoading] = useState(false);
  const [workers, setWorkers] = useState([]);
  const [reviewing, setReviewing] = useState(false);
  const [approveCandidate, setApproveCandidate] = useState(null);
  const [rejectCandidate, setRejectCandidate] = useState(null);
  const [dismissCandidate, setDismissCandidate] = useState(null);
  const [approveForm, setApproveForm] = useState(EMPTY_APPROVE_FORM);
  const [rejectReason, setRejectReason] = useState('');
  const [dismissComment, setDismissComment] = useState('');
  const [createdInspectionId, setCreatedInspectionId] = useState(null);

  const canView = canViewPreventiveExecutionCandidates(auth?.user?.role);
  const canGenerate = canGeneratePreventiveExecutionCandidates(auth?.user?.role);
  const canReview = canReviewPreventiveExecutionCandidates(auth?.user?.role);
  const canExport = canExportReporting(auth?.user?.role);

  const selectedAsset = useMemo(
    () => assets.find((asset) => asset.id === approveCandidate?.assetId),
    [assets, approveCandidate]
  );

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

  const buildFilters = () => ({
    status: filterStatus || undefined,
    assetId: filterAssetId ? Number(filterAssetId) : undefined,
    planId: filterPlanId ? Number(filterPlanId) : undefined,
  });

  const loadCandidatesWithFilters = async (page, statusFilter, assetFilter, planFilter) => {
    try {
      setListLoading(true);
      setError(null);
      const filters = {
        status: statusFilter || undefined,
        assetId: assetFilter ? Number(assetFilter) : undefined,
        planId: planFilter ? Number(planFilter) : undefined,
      };
      const candidatePage = await preventiveExecutionCandidateApi.list(page, undefined, filters);
      setCandidates(unwrapPageContent(candidatePage));
      setCandidatesPage(getPageNumber(candidatePage, page));
      setCandidatesTotalPages(getTotalPages(candidatePage));
    } catch (err) {
      setError(getApiErrorMessage(err, 'Failed to load preventive execution candidates.'));
    } finally {
      setListLoading(false);
    }
  };

  const loadPageData = async (page = candidatesPage) => {
    try {
      setLoading(true);
      setError(null);
      const [candidatePage, assetPage, planPage] = await Promise.all([
        preventiveExecutionCandidateApi.list(page, undefined, buildFilters()),
        assetApi.list(DEFAULT_PAGE, MAX_PAGE_SIZE),
        preventiveMaintenancePlanApi.list(DEFAULT_PAGE, MAX_PAGE_SIZE),
      ]);
      setCandidates(unwrapPageContent(candidatePage));
      setCandidatesPage(getPageNumber(candidatePage, page));
      setCandidatesTotalPages(getTotalPages(candidatePage));
      setAssets(unwrapPageContent(assetPage));
      setPlans(unwrapPageContent(planPage));
      if (canReview) {
        const profile = await userApi.getCurrentUser();
        const workerData = await inspectionApi.listWorkers();
        const departmentId = selectedAsset?.departmentId ?? profile?.departmentId;
        setWorkers(filterInspectionAssignees(workerData, departmentId));
      }
    } catch (err) {
      setError(getApiErrorMessage(err, 'Failed to load preventive execution candidates.'));
    } finally {
      setLoading(false);
      setListLoading(false);
    }
  };

  const handleFilterChange = (e) => {
    const { name, value } = e.target;
    const nextStatus = name === 'filterStatus' ? value : filterStatus;
    const nextAsset = name === 'filterAssetId' ? value : filterAssetId;
    const nextPlan = name === 'filterPlanId' ? value : filterPlanId;
    if (name === 'filterStatus') setFilterStatus(value);
    else if (name === 'filterAssetId') setFilterAssetId(value);
    else if (name === 'filterPlanId') setFilterPlanId(value);
    loadCandidatesWithFilters(DEFAULT_PAGE, nextStatus, nextAsset, nextPlan);
  };

  const handleGenerate = async () => {
    if (!canGenerate) return;

    try {
      setGenerating(true);
      setError(null);
      setSuccess(null);
      const results = await preventiveExecutionCandidateApi.generate();
      const created = results.filter((result) => result.outcome === 'CREATED').length;
      const skipped = results.filter((result) => result.outcome === 'SKIPPED_DUPLICATE').length;
      setSuccess(
        `Generation complete: ${created} created, ${skipped} skipped (existing pending), ` +
          `${results.length - created - skipped} not eligible.`
      );
      await loadCandidatesWithFilters(DEFAULT_PAGE, filterStatus, filterAssetId, filterPlanId);
    } catch (err) {
      if (isForbidden(err)) {
        setError('You do not have permission to generate preventive execution candidates.');
      } else {
        setError(getApiErrorMessage(err, 'Failed to generate preventive execution candidates.'));
      }
    } finally {
      setGenerating(false);
    }
  };

  const handleViewDetail = async (candidateId) => {
    try {
      setDetailLoading(true);
      setError(null);
      setDetailTab('candidate');
      const [detail, report] = await Promise.all([
        preventiveExecutionCandidateApi.get(candidateId),
        preventiveExecutionCandidateApi.getReport(candidateId),
      ]);
      setSelectedCandidate(detail);
      setSelectedReport(report);
    } catch (err) {
      setError(getApiErrorMessage(err, 'Failed to load candidate details.'));
    } finally {
      setDetailLoading(false);
    }
  };

  const reloadSelectedDetail = async (candidateId) => {
    const [detail, report] = await Promise.all([
      preventiveExecutionCandidateApi.get(candidateId),
      preventiveExecutionCandidateApi.getReport(candidateId),
    ]);
    setSelectedCandidate(detail);
    setSelectedReport(report);
  };

  const openApproveDialog = async (candidate) => {
    setApproveCandidate(candidate);
    setApproveForm(EMPTY_APPROVE_FORM);
    setCreatedInspectionId(null);
    if (canReview) {
      try {
        const profile = await userApi.getCurrentUser();
        const asset = assets.find((item) => item.id === candidate.assetId);
        const workerData = await inspectionApi.listWorkers();
        setWorkers(
          filterInspectionAssignees(workerData, asset?.departmentId ?? profile?.departmentId)
        );
      } catch {
        setWorkers([]);
      }
    }
  };

  const selectedAssigneeId = parseAssigneeId(approveForm.assigneeId);

  const handleApproveSubmit = async (e) => {
    e.preventDefault();
    if (!approveCandidate || !canReview || selectedAssigneeId == null) return;

    try {
      setReviewing(true);
      setError(null);
      setSuccess(null);
      const response = await preventiveExecutionCandidateApi.approve(approveCandidate.id, {
        assigneeId: selectedAssigneeId,
        plannedAt: dateInputToPlannedAt(approveForm.plannedAt),
        notes: approveForm.notes.trim() || undefined,
      });
      setCreatedInspectionId(response.inspection?.id ?? response.candidate?.createdInspectionId);
      setSuccess('Candidate approved and inspection created.');
      setApproveCandidate(null);
      setSelectedCandidate(response.candidate);
      await reloadSelectedDetail(response.candidate.id);
      await loadCandidatesWithFilters(candidatesPage, filterStatus, filterAssetId, filterPlanId);
    } catch (err) {
      setError(getApiErrorMessage(err, 'Failed to approve preventive execution candidate.'));
    } finally {
      setReviewing(false);
    }
  };

  const handleRejectSubmit = async (e) => {
    e.preventDefault();
    if (!rejectCandidate || !canReview) return;

    try {
      setReviewing(true);
      setError(null);
      setSuccess(null);
      const response = await preventiveExecutionCandidateApi.reject(rejectCandidate.id, {
        reason: rejectReason.trim() || undefined,
      });
      setSuccess('Candidate rejected.');
      setRejectCandidate(null);
      setRejectReason('');
      setSelectedCandidate(response);
      await reloadSelectedDetail(response.id);
      await loadCandidatesWithFilters(candidatesPage, filterStatus, filterAssetId, filterPlanId);
    } catch (err) {
      setError(getApiErrorMessage(err, 'Failed to reject preventive execution candidate.'));
    } finally {
      setReviewing(false);
    }
  };

  const handleDismissSubmit = async (e) => {
    e.preventDefault();
    if (!dismissCandidate || !canReview) return;

    try {
      setReviewing(true);
      setError(null);
      setSuccess(null);
      const response = await preventiveExecutionCandidateApi.dismiss(dismissCandidate.id, {
        comment: dismissComment.trim() || undefined,
      });
      setSuccess('Candidate dismissed.');
      setDismissCandidate(null);
      setDismissComment('');
      setSelectedCandidate(response);
      await reloadSelectedDetail(response.id);
      await loadCandidatesWithFilters(candidatesPage, filterStatus, filterAssetId, filterPlanId);
    } catch (err) {
      setError(getApiErrorMessage(err, 'Failed to dismiss preventive execution candidate.'));
    } finally {
      setReviewing(false);
    }
  };

  const renderReviewActions = (candidate) => {
    if (!canReview || candidate.candidateStatus !== 'PENDING') {
      return null;
    }
    return (
      <>
        {' '}
        <button type="button" className="btn-link" onClick={() => openApproveDialog(candidate)}>
          Approve
        </button>{' '}
        <button
          type="button"
          className="btn-link"
          onClick={() => {
            setRejectCandidate(candidate);
            setRejectReason('');
          }}
        >
          Reject
        </button>{' '}
        <button
          type="button"
          className="btn-link"
          onClick={() => {
            setDismissCandidate(candidate);
            setDismissComment('');
          }}
        >
          Dismiss
        </button>
      </>
    );
  };

  if (loading) {
    return <div className="loading">Loading preventive execution candidates...</div>;
  }

  return (
    <ReferenceDataLayout
      title="Preventive Execution Candidates"
      headerActions={
        canExport ? (
          <>
            <ExportCsvButton
              exportType={REPORTING_EXPORT_TYPES.PREVENTIVE_CANDIDATES}
              onError={setError}
            />
            <ExportReportingButton
              exportType={REPORTING_EXPORT_TYPES.PREVENTIVE_CANDIDATES}
              format={REPORTING_EXPORT_FORMATS.XLSX}
              onError={setError}
            />
          </>
        ) : null
      }
    >
      {error && <div className="error-message">{error}</div>}
      {success && <div className="success-message">{success}</div>}

      <section className="reference-form-section">
        <div className="section-header">
          <h2>Candidate Queue</h2>
          {canGenerate && (
            <button
              type="button"
              className="btn-primary"
              onClick={handleGenerate}
              disabled={generating}
            >
              {generating ? 'Generating...' : 'Generate Candidates'}
            </button>
          )}
        </div>

        <p className="section-description">
          Review eligible preventive maintenance plans and decide whether to create inspections.
          Automation and scheduling remain out of scope.
        </p>

        <div className="filter-row">
          <label htmlFor="filterStatus">
            Status
            <select
              id="filterStatus"
              name="filterStatus"
              value={filterStatus}
              onChange={handleFilterChange}
            >
              {EXECUTION_CANDIDATE_STATUS_OPTIONS.map((option) => (
                <option key={option.value || 'all'} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </label>
          <label htmlFor="filterAssetId">
            Asset
            <select
              id="filterAssetId"
              name="filterAssetId"
              value={filterAssetId}
              onChange={handleFilterChange}
            >
              <option value="">All assets</option>
              {assets.map((asset) => (
                <option key={asset.id} value={asset.id}>
                  {asset.name}
                </option>
              ))}
            </select>
          </label>
          <label htmlFor="filterPlanId">
            Plan
            <select
              id="filterPlanId"
              name="filterPlanId"
              value={filterPlanId}
              onChange={handleFilterChange}
            >
              <option value="">All plans</option>
              {plans.map((plan) => (
                <option key={plan.id} value={plan.id}>
                  {plan.planCode} —{plan.name}
                </option>
              ))}
            </select>
          </label>
        </div>

        {listLoading ? (
          <p>Loading candidates...</p>
        ) : (
          <table className="reference-table">
            <thead>
              <tr>
                <th>Plan Code</th>
                <th>Plan Name</th>
                <th>Asset</th>
                <th>Trigger</th>
                <th>Target Action</th>
                <th>Status</th>
                <th>Evaluated</th>
                <th>Next Eligible</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {candidates.length === 0 ? (
                <tr>
                  <td colSpan={9}>No execution candidates found.</td>
                </tr>
              ) : (
                candidates.map((candidate) => (
                  <tr key={candidate.id}>
                    <td>{candidate.planCodeSnapshot}</td>
                    <td>{candidate.planNameSnapshot}</td>
                    <td>{candidate.assetName}</td>
                    <td>{candidate.triggerType}</td>
                    <td>{getPlanTargetActionLabel(candidate.targetActionSnapshot)}</td>
                    <td>{getExecutionCandidateStatusLabel(candidate.candidateStatus)}</td>
                    <td>{formatTimestamp(candidate.evaluatedAt)}</td>
                    <td>{formatTimestamp(candidate.nextEligibleAt)}</td>
                    <td>
                      <button
                        type="button"
                        className="btn-link"
                        onClick={() => handleViewDetail(candidate.id)}
                      >
                        View
                      </button>
                      {renderReviewActions(candidate)}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        )}
      </section>

      <PaginationControls
        page={candidatesPage}
        totalPages={candidatesTotalPages}
        loading={listLoading}
        onPrevious={() =>
          loadCandidatesWithFilters(candidatesPage - 1, filterStatus, filterAssetId, filterPlanId)
        }
        onNext={() =>
          loadCandidatesWithFilters(candidatesPage + 1, filterStatus, filterAssetId, filterPlanId)
        }
      />

      {selectedCandidate && (
        <section className="reference-form-section candidate-detail-section">
          <div className="detail-panel-header">
            <h2>Candidate Detail</h2>
            <button
              type="button"
              className="btn-secondary"
              onClick={() => {
                setSelectedCandidate(null);
                setSelectedReport(null);
              }}
            >
              Close
            </button>
          </div>
          <div className="detail-tab-bar" role="tablist" aria-label="Candidate detail views">
            <button
              type="button"
              role="tab"
              aria-selected={detailTab === 'candidate'}
              className={`detail-tab${detailTab === 'candidate' ? ' detail-tab-active' : ''}`}
              onClick={() => setDetailTab('candidate')}
            >
              Candidate
            </button>
            <button
              type="button"
              role="tab"
              aria-selected={detailTab === 'report'}
              className={`detail-tab${detailTab === 'report' ? ' detail-tab-active' : ''}`}
              onClick={() => setDetailTab('report')}
            >
              Execution Report
            </button>
          </div>
          {detailLoading ? (
            <p>Loading detail...</p>
          ) : detailTab === 'candidate' ? (
            <dl className="detail-list">
              <dt>Plan Code</dt>
              <dd>{selectedCandidate.planCodeSnapshot}</dd>
              <dt>Plan Name</dt>
              <dd>{selectedCandidate.planNameSnapshot}</dd>
              <dt>Plan Version</dt>
              <dd>{selectedCandidate.planVersionSnapshot}</dd>
              <dt>Asset</dt>
              <dd>{selectedCandidate.assetName}</dd>
              <dt>Trigger Type</dt>
              <dd>{selectedCandidate.triggerType}</dd>
              <dt>Trigger Summary</dt>
              <dd>
                <strong>{selectedCandidate.triggerSummaryTitleSnapshot}</strong>
                <div>{selectedCandidate.triggerSummaryDescriptionSnapshot}</div>
              </dd>
              <dt>Target Action</dt>
              <dd>{getPlanTargetActionLabel(selectedCandidate.targetActionSnapshot)}</dd>
              <dt>Eligibility Reason</dt>
              <dd>{selectedCandidate.eligibilityReason}</dd>
              <dt>Status</dt>
              <dd>{getExecutionCandidateStatusLabel(selectedCandidate.candidateStatus)}</dd>
              {selectedCandidate.createdInspectionId && (
                <>
                  <dt>Created Inspection</dt>
                  <dd>
                    <Link to={ROUTES.INSPECTIONS}>
                      Inspection #{selectedCandidate.createdInspectionId}
                    </Link>
                  </dd>
                </>
              )}
              {selectedCandidate.rejectionReason && (
                <>
                  <dt>Rejection Reason</dt>
                  <dd>{selectedCandidate.rejectionReason}</dd>
                </>
              )}
              {selectedCandidate.dismissComment && (
                <>
                  <dt>Dismiss Comment</dt>
                  <dd>{selectedCandidate.dismissComment}</dd>
                </>
              )}
              {selectedCandidate.decisionNotes && (
                <>
                  <dt>Decision Notes</dt>
                  <dd>{selectedCandidate.decisionNotes}</dd>
                </>
              )}
              <dt>Evaluated At</dt>
              <dd>{formatTimestamp(selectedCandidate.evaluatedAt)}</dd>
              <dt>Next Eligible At</dt>
              <dd>{formatTimestamp(selectedCandidate.nextEligibleAt)}</dd>
              <dt>Created At</dt>
              <dd>{formatTimestamp(selectedCandidate.createdAt)}</dd>
            </dl>
          ) : selectedReport ? (
            <dl className="detail-list execution-report-detail">
              <dt>Report Status</dt>
              <dd>{getExecutionReportStatusLabel(selectedReport.reportStatus)}</dd>
              <dt>Decision Source</dt>
              <dd>{getDecisionSourceLabel(selectedReport.decisionSource)}</dd>
              <dt>Generated At</dt>
              <dd>{formatTimestamp(selectedReport.generatedAt)}</dd>
              <dt>Approved At</dt>
              <dd>{formatTimestamp(selectedReport.approvedAt)}</dd>
              <dt>Rejected At</dt>
              <dd>{formatTimestamp(selectedReport.rejectedAt)}</dd>
              <dt>Dismissed At</dt>
              <dd>{formatTimestamp(selectedReport.dismissedAt)}</dd>
              <dt>Inspection Created At</dt>
              <dd>{formatTimestamp(selectedReport.inspectionCreatedAt)}</dd>
              {selectedReport.createdInspectionId && (
                <>
                  <dt>Created Inspection</dt>
                  <dd>
                    <Link to={ROUTES.INSPECTIONS}>
                      Inspection #{selectedReport.createdInspectionId}
                    </Link>
                  </dd>
                </>
              )}
              {selectedReport.decisionReason && (
                <>
                  <dt>Decision Reason</dt>
                  <dd>{selectedReport.decisionReason}</dd>
                </>
              )}
              <dt>Plan Code</dt>
              <dd>{selectedReport.planCodeSnapshot}</dd>
              <dt>Asset</dt>
              <dd>{selectedReport.assetNameSnapshot}</dd>
            </dl>
          ) : (
            <p>No execution report available.</p>
          )}
          {!detailLoading &&
            canReview &&
            selectedCandidate.candidateStatus === PREVENTIVE_CANDIDATE_STATUS.PENDING && (
              <div className="review-actions">
                <button
                  type="button"
                  className="btn-primary"
                  onClick={() => openApproveDialog(selectedCandidate)}
                >
                  Approve
                </button>{' '}
                <button
                  type="button"
                  className="btn-secondary"
                  onClick={() => {
                    setRejectCandidate(selectedCandidate);
                    setRejectReason('');
                  }}
                >
                  Reject
                </button>{' '}
                <button
                  type="button"
                  className="btn-secondary"
                  onClick={() => {
                    setDismissCandidate(selectedCandidate);
                    setDismissComment('');
                  }}
                >
                  Dismiss
                </button>
              </div>
            )}
        </section>
      )}

      {approveCandidate && (
        <section className="reference-form-section dialog-panel">
          <h2>Approve Candidate</h2>
          <p>
            Plan: <strong>{approveCandidate.planCodeSnapshot}</strong> —{approveCandidate.assetName}
          </p>
          <p>{approveCandidate.eligibilityReason}</p>
          <form onSubmit={handleApproveSubmit}>
            <label htmlFor="approveAssigneeId">
              Assignee
              <select
                id="approveAssigneeId"
                value={approveForm.assigneeId}
                onChange={(e) =>
                  setApproveForm((prev) => ({
                    ...prev,
                    assigneeId: e.target.value,
                  }))
                }
                required
              >
                <option value="">Select field employee</option>
                {workers.map((worker) => (
                  <option key={worker.id} value={worker.id}>
                    {worker.name}
                  </option>
                ))}
              </select>
            </label>
            <label htmlFor="approvePlannedAt">
              Planned Date
              <input
                id="approvePlannedAt"
                type="date"
                value={approveForm.plannedAt}
                onChange={(e) =>
                  setApproveForm((prev) => ({
                    ...prev,
                    plannedAt: e.target.value,
                  }))
                }
              />
            </label>
            <label htmlFor="approveNotes">
              Notes
              <textarea
                id="approveNotes"
                value={approveForm.notes}
                onChange={(e) =>
                  setApproveForm((prev) => ({
                    ...prev,
                    notes: e.target.value,
                  }))
                }
                rows={3}
              />
            </label>
            <div className="form-actions">
              <button
                type="submit"
                className="btn-primary"
                disabled={reviewing || selectedAssigneeId == null}
              >
                {reviewing ? 'Approving...' : 'Approve and Create Inspection'}
              </button>{' '}
              <button
                type="button"
                className="btn-secondary"
                onClick={() => setApproveCandidate(null)}
              >
                Cancel
              </button>
            </div>
          </form>
          {createdInspectionId && (
            <p>
              Inspection created: <Link to={ROUTES.INSPECTIONS}>#{createdInspectionId}</Link>
            </p>
          )}
        </section>
      )}

      {rejectCandidate && (
        <section className="reference-form-section dialog-panel">
          <h2>Reject Candidate</h2>
          <p>
            Plan: <strong>{rejectCandidate.planCodeSnapshot}</strong>
          </p>
          <form onSubmit={handleRejectSubmit}>
            <label htmlFor="rejectReason">
              Reason
              <textarea
                id="rejectReason"
                value={rejectReason}
                onChange={(e) => setRejectReason(e.target.value)}
                rows={3}
              />
            </label>
            <div className="form-actions">
              <button type="submit" className="btn-primary" disabled={reviewing}>
                {reviewing ? 'Rejecting...' : 'Reject Candidate'}
              </button>{' '}
              <button
                type="button"
                className="btn-secondary"
                onClick={() => setRejectCandidate(null)}
              >
                Cancel
              </button>
            </div>
          </form>
        </section>
      )}

      {dismissCandidate && (
        <section className="reference-form-section dialog-panel">
          <h2>Dismiss Candidate</h2>
          <p>
            Plan: <strong>{dismissCandidate.planCodeSnapshot}</strong>
          </p>
          <form onSubmit={handleDismissSubmit}>
            <label htmlFor="dismissComment">
              Comment
              <textarea
                id="dismissComment"
                value={dismissComment}
                onChange={(e) => setDismissComment(e.target.value)}
                rows={3}
              />
            </label>
            <div className="form-actions">
              <button type="submit" className="btn-primary" disabled={reviewing}>
                {reviewing ? 'Dismissing...' : 'Dismiss Candidate'}
              </button>{' '}
              <button
                type="button"
                className="btn-secondary"
                onClick={() => setDismissCandidate(null)}
              >
                Cancel
              </button>
            </div>
          </form>
        </section>
      )}
    </ReferenceDataLayout>
  );
}
