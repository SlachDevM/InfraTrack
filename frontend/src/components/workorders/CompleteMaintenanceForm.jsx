import { getOperationalDecisionOutcomeLabel } from '../../constants/operationalDecisionOutcomes';

export default function CompleteMaintenanceForm({
  completeFormData,
  assignedWorkOrdersForCurrentUser,
  selectedCompleteWorkOrder,
  completing,
  onChange,
  onSubmit,
}) {
  return (
    <section className="work-order-form-section">
      <h2>Complete Maintenance Activity</h2>
      <form className="work-order-form" onSubmit={onSubmit}>
        <div className="form-row">
          <label htmlFor="completeWorkOrderId">Assigned Work Order</label>
          <select
            id="completeWorkOrderId"
            name="workOrderId"
            value={completeFormData.workOrderId}
            onChange={onChange}
            required
            disabled={completing || assignedWorkOrdersForCurrentUser.length === 0}
          >
            <option value="">Select assigned work order</option>
            {assignedWorkOrdersForCurrentUser.map((workOrder) => (
              <option key={workOrder.id} value={workOrder.id}>
                #{workOrder.id} — {workOrder.assetName} (
                {getOperationalDecisionOutcomeLabel(workOrder.workType)})
              </option>
            ))}
          </select>
        </div>

        {selectedCompleteWorkOrder && (
          <div className="linked-decision-info">
            <strong>Asset:</strong> {selectedCompleteWorkOrder.assetName}
            <br />
            <strong>Description:</strong> {selectedCompleteWorkOrder.description}
          </div>
        )}

        <div className="form-row">
          <label htmlFor="completionNotes">Completion Notes</label>
          <textarea
            id="completionNotes"
            name="completionNotes"
            value={completeFormData.completionNotes}
            onChange={onChange}
            required
            disabled={completing}
            rows={3}
          />
        </div>

        <div className="form-row">
          <label htmlFor="completedAt">Completion Date & Time</label>
          <input
            id="completedAt"
            name="completedAt"
            type="datetime-local"
            value={completeFormData.completedAt}
            onChange={onChange}
            required
            disabled={completing}
          />
        </div>

        <button
          type="submit"
          className="btn-primary"
          disabled={completing || assignedWorkOrdersForCurrentUser.length === 0}
        >
          {completing ? 'Completing...' : 'Complete Maintenance'}
        </button>
      </form>
      {assignedWorkOrdersForCurrentUser.length === 0 && (
        <p className="read-only-note">
          You have no assigned work orders awaiting maintenance completion.
        </p>
      )}
    </section>
  );
}
