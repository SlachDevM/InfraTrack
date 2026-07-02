import { getOperationalDecisionOutcomeLabel } from '../../constants/operationalDecisionOutcomes';
import { WORK_ORDER_PRIORITY_OPTIONS } from '../../constants/workOrderPriorities';

export default function CreateWorkOrderForm({
  formData,
  eligibleDecisions,
  selectedDecision,
  submitting,
  onChange,
  onSubmit,
}) {
  return (
    <section className="work-order-form-section">
      <h2>Create Work Order</h2>
      <form className="work-order-form" onSubmit={onSubmit}>
        <div className="form-row">
          <label htmlFor="operationalDecisionId">Operational Decision</label>
          <select
            id="operationalDecisionId"
            name="operationalDecisionId"
            value={formData.operationalDecisionId}
            onChange={onChange}
            required
            disabled={submitting || eligibleDecisions.length === 0}
          >
            <option value="">Select operational decision</option>
            {eligibleDecisions.map((decision) => (
              <option key={decision.id} value={decision.id}>
                #{decision.id} — {decision.assetName} (
                {getOperationalDecisionOutcomeLabel(decision.outcome)})
              </option>
            ))}
          </select>
        </div>

        {selectedDecision && (
          <div className="linked-decision-info">
            <strong>Asset:</strong> {selectedDecision.assetName}
            <br />
            <strong>Outcome:</strong> {getOperationalDecisionOutcomeLabel(selectedDecision.outcome)}
            <br />
            <strong>Rationale:</strong> {selectedDecision.rationale}
          </div>
        )}

        <div className="form-row">
          <label htmlFor="description">Description</label>
          <textarea
            id="description"
            name="description"
            value={formData.description}
            onChange={onChange}
            required
            disabled={submitting}
            rows={3}
          />
        </div>

        <div className="form-row">
          <label htmlFor="priority">Priority</label>
          <select
            id="priority"
            name="priority"
            value={formData.priority}
            onChange={onChange}
            required
            disabled={submitting}
          >
            {WORK_ORDER_PRIORITY_OPTIONS.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </div>

        <div className="form-row">
          <label htmlFor="createdAtBusinessDate">Creation Date & Time</label>
          <input
            id="createdAtBusinessDate"
            name="createdAtBusinessDate"
            type="datetime-local"
            value={formData.createdAtBusinessDate}
            onChange={onChange}
            required
            disabled={submitting}
          />
        </div>

        <button
          type="submit"
          className="btn-primary"
          disabled={submitting || eligibleDecisions.length === 0}
        >
          {submitting ? 'Creating...' : 'Create Work Order'}
        </button>
      </form>
      {eligibleDecisions.length === 0 && (
        <p className="read-only-note">
          No operational decisions authorising physical work are awaiting a work order.
        </p>
      )}
    </section>
  );
}
