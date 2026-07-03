import { Link } from 'react-router-dom';
import { PREVENTIVE_CANDIDATE_STATUS } from '../../constants/statuses';
import { ROUTES } from '../../constants/routes';
import { getExecutionCandidateStatusLabel } from '../../constants/executionCandidateStatuses';
import {
  getDecisionSourceLabel,
  getExecutionReportStatusLabel,
} from '../../constants/executionReportStatuses';
import { getPlanTargetActionLabel } from '../../constants/planTargetActions';
import { formatTimestamp } from '../../pages/preventiveExecutionCandidates/constants';

export default function PreventiveCandidateDetailPanel({
  selectedCandidate,
  selectedReport,
  detailTab,
  detailLoading,
  canReview,
  onTabChange,
  onClose,
  onApprove,
  onReject,
  onDismiss,
}) {
  return (
    <section className="reference-form-section candidate-detail-section">
      <div className="detail-panel-header">
        <h2>Candidate Detail</h2>
        <button type="button" className="btn-secondary" onClick={onClose}>
          Close
        </button>
      </div>
      <div className="detail-tab-bar" role="tablist" aria-label="Candidate detail views">
        <button
          type="button"
          role="tab"
          aria-selected={detailTab === 'candidate'}
          className={`detail-tab${detailTab === 'candidate' ? ' detail-tab-active' : ''}`}
          onClick={() => onTabChange('candidate')}
        >
          Candidate
        </button>
        <button
          type="button"
          role="tab"
          aria-selected={detailTab === 'report'}
          className={`detail-tab${detailTab === 'report' ? ' detail-tab-active' : ''}`}
          onClick={() => onTabChange('report')}
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
              onClick={() => onApprove(selectedCandidate)}
            >
              Approve
            </button>{' '}
            <button
              type="button"
              className="btn-secondary"
              onClick={() => onReject(selectedCandidate)}
            >
              Reject
            </button>{' '}
            <button
              type="button"
              className="btn-secondary"
              onClick={() => onDismiss(selectedCandidate)}
            >
              Dismiss
            </button>
          </div>
        )}
    </section>
  );
}
