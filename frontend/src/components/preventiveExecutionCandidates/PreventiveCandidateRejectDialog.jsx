export default function PreventiveCandidateRejectDialog({
  rejectCandidate,
  rejectReason,
  reviewing,
  onSubmit,
  onCancel,
  onReasonChange,
}) {
  return (
    <section className="reference-form-section dialog-panel">
      <h2>Reject Candidate</h2>
      <p>
        Plan: <strong>{rejectCandidate.planCodeSnapshot}</strong>
      </p>
      <form onSubmit={onSubmit}>
        <label htmlFor="rejectReason">
          Reason
          <textarea
            id="rejectReason"
            value={rejectReason}
            onChange={(e) => onReasonChange(e.target.value)}
            rows={3}
          />
        </label>
        <div className="form-actions">
          <button type="submit" className="btn-primary" disabled={reviewing}>
            {reviewing ? 'Rejecting...' : 'Reject Candidate'}
          </button>{' '}
          <button type="button" className="btn-secondary" onClick={onCancel}>
            Cancel
          </button>
        </div>
      </form>
    </section>
  );
}
