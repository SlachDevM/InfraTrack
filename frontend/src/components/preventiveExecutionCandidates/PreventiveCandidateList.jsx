import { getExecutionCandidateStatusLabel } from '../../constants/executionCandidateStatuses';
import { getPlanTargetActionLabel } from '../../constants/planTargetActions';
import { formatTimestamp } from '../../pages/preventiveExecutionCandidates/constants';

function CandidateReviewActions({ candidate, canReview, onApprove, onReject, onDismiss }) {
  if (!canReview || candidate.candidateStatus !== 'PENDING') {
    return null;
  }
  return (
    <>
      {' '}
      <button type="button" className="btn-link" onClick={() => onApprove(candidate)}>
        Approve
      </button>{' '}
      <button type="button" className="btn-link" onClick={() => onReject(candidate)}>
        Reject
      </button>{' '}
      <button type="button" className="btn-link" onClick={() => onDismiss(candidate)}>
        Dismiss
      </button>
    </>
  );
}

export default function PreventiveCandidateList({
  candidates,
  listLoading,
  canReview,
  onViewDetail,
  onApprove,
  onReject,
  onDismiss,
}) {
  if (listLoading) {
    return <p>Loading candidates...</p>;
  }

  return (
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
                  onClick={() => onViewDetail(candidate.id)}
                >
                  View
                </button>
                <CandidateReviewActions
                  candidate={candidate}
                  canReview={canReview}
                  onApprove={onApprove}
                  onReject={onReject}
                  onDismiss={onDismiss}
                />
              </td>
            </tr>
          ))
        )}
      </tbody>
    </table>
  );
}
