import { COMPLETION_REVIEW_DECISION_OPTIONS } from '../../constants/completionReviewDecisions';
import { ISSUE_SEVERITY_OPTIONS } from '../../constants/issueSeverities';
import { formatTimestamp } from '../../utils/dateTime';

export default function CompletionReviewForm({
  reviewFormData,
  reviewableMaintenanceActivities,
  selectedReviewActivity,
  isReworkRequired,
  reviewing,
  onChange,
  onSubmit,
}) {
  return (
    <section className="work-order-form-section">
      <h2>Record Completion Review</h2>
      <form className="work-order-form" onSubmit={onSubmit}>
        <div className="form-row">
          <label htmlFor="maintenanceActivityId">Maintenance Activity</label>
          <select
            id="maintenanceActivityId"
            name="maintenanceActivityId"
            value={reviewFormData.maintenanceActivityId}
            onChange={onChange}
            required
            disabled={reviewing || reviewableMaintenanceActivities.length === 0}
          >
            <option value="">Select maintenance activity</option>
            {reviewableMaintenanceActivities.map((activity) => (
              <option key={activity.id} value={activity.id}>
                #{activity.id} — WO #{activity.workOrderId} — {activity.assetName}
              </option>
            ))}
          </select>
        </div>

        {selectedReviewActivity && (
          <div className="linked-decision-info">
            <strong>Asset:</strong> {selectedReviewActivity.assetName}
            <br />
            <strong>Completion Notes:</strong> {selectedReviewActivity.completionNotes}
            <br />
            <strong>Completed:</strong> {formatTimestamp(selectedReviewActivity.completedAt)}
          </div>
        )}

        <div className="form-row">
          <label htmlFor="decision">Review Decision</label>
          <select
            id="decision"
            name="decision"
            value={reviewFormData.decision}
            onChange={onChange}
            required
            disabled={reviewing}
          >
            {COMPLETION_REVIEW_DECISION_OPTIONS.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </div>

        <div className="form-row">
          <label htmlFor="reviewNotes">Review Notes</label>
          <textarea
            id="reviewNotes"
            name="reviewNotes"
            value={reviewFormData.reviewNotes}
            onChange={onChange}
            required
            disabled={reviewing}
            rows={3}
          />
        </div>

        {isReworkRequired && (
          <>
            <div className="form-row">
              <label htmlFor="reworkSeverity">Rework Severity</label>
              <select
                id="reworkSeverity"
                name="reworkSeverity"
                value={reviewFormData.reworkSeverity}
                onChange={onChange}
                required
                disabled={reviewing}
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
                value={reviewFormData.rootCause}
                onChange={onChange}
                disabled={reviewing}
                rows={2}
              />
            </div>

            <div className="form-row">
              <label htmlFor="correctiveAction">Corrective Action</label>
              <textarea
                id="correctiveAction"
                name="correctiveAction"
                value={reviewFormData.correctiveAction}
                onChange={onChange}
                disabled={reviewing}
                rows={2}
              />
            </div>

            <div className="form-row">
              <label htmlFor="preventiveAction">Preventive Action</label>
              <textarea
                id="preventiveAction"
                name="preventiveAction"
                value={reviewFormData.preventiveAction}
                onChange={onChange}
                disabled={reviewing}
                rows={2}
              />
            </div>
          </>
        )}

        <div className="form-row">
          <label htmlFor="reviewedAt">Review Date & Time</label>
          <input
            id="reviewedAt"
            name="reviewedAt"
            type="datetime-local"
            value={reviewFormData.reviewedAt}
            onChange={onChange}
            required
            disabled={reviewing}
          />
        </div>

        <button
          type="submit"
          className="btn-primary"
          disabled={reviewing || reviewableMaintenanceActivities.length === 0}
        >
          {reviewing ? 'Recording...' : 'Record Completion Review'}
        </button>
      </form>
      {reviewableMaintenanceActivities.length === 0 && (
        <p className="read-only-note">
          No completed maintenance activities are awaiting completion review.
        </p>
      )}
    </section>
  );
}
