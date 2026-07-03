export default function PreventiveCandidateDismissDialog({
  dismissCandidate,
  dismissComment,
  reviewing,
  onSubmit,
  onCancel,
  onCommentChange,
}) {
  return (
    <section className="reference-form-section dialog-panel">
      <h2>Dismiss Candidate</h2>
      <p>
        Plan: <strong>{dismissCandidate.planCodeSnapshot}</strong>
      </p>
      <form onSubmit={onSubmit}>
        <label htmlFor="dismissComment">
          Comment
          <textarea
            id="dismissComment"
            value={dismissComment}
            onChange={(e) => onCommentChange(e.target.value)}
            rows={3}
          />
        </label>
        <div className="form-actions">
          <button type="submit" className="btn-primary" disabled={reviewing}>
            {reviewing ? 'Dismissing...' : 'Dismiss Candidate'}
          </button>{' '}
          <button type="button" className="btn-secondary" onClick={onCancel}>
            Cancel
          </button>
        </div>
      </form>
    </section>
  );
}
