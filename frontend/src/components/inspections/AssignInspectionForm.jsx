import { getBusinessTriggerTypeLabel } from '../../constants/businessTriggerTypes';
import {
  INSPECTION_PRIORITY_OPTIONS,
} from '../../constants/inspectionPriorities';

export default function AssignInspectionForm({
  formData,
  triggers,
  workers,
  selectedTrigger,
  submitting,
  onChange,
  onSubmit,
}) {
  return (
    <section className="inspection-form-section">
      <h2>Assign Inspection</h2>
      <form className="inspection-form" onSubmit={onSubmit}>
        <div className="form-row">
          <label htmlFor="businessTriggerId">Business Trigger</label>
          <select
            id="businessTriggerId"
            name="businessTriggerId"
            value={formData.businessTriggerId}
            onChange={onChange}
            required
            disabled={submitting || triggers.length === 0}
          >
            <option value="">Select business trigger</option>
            {triggers.map((trigger) => (
              <option key={trigger.id} value={trigger.id}>
                #{trigger.id} — {trigger.assetName} ({getBusinessTriggerTypeLabel(trigger.type)})
              </option>
            ))}
          </select>
        </div>

        {selectedTrigger && (
          <div className="linked-asset-info">
            <strong>Linked asset:</strong> {selectedTrigger.assetName}
            <br />
            <strong>Reason:</strong> {selectedTrigger.reason}
            {selectedTrigger.urgent && (
              <>
                <br />
                <strong>Urgent trigger</strong>
              </>
            )}
          </div>
        )}

        <div className="form-row">
          <label htmlFor="assignedToUserId">Assign To</label>
          <select
            id="assignedToUserId"
            name="assignedToUserId"
            value={formData.assignedToUserId}
            onChange={onChange}
            required
            disabled={submitting || workers.length === 0}
          >
            <option value="">Select worker</option>
            {workers.map((worker) => (
              <option key={worker.id} value={worker.id}>
                {worker.name}
              </option>
            ))}
          </select>
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
            {INSPECTION_PRIORITY_OPTIONS.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </div>

        <div className="form-row">
          <label htmlFor="expectedCompletionDate">Expected Completion Date</label>
          <input
            id="expectedCompletionDate"
            name="expectedCompletionDate"
            type="date"
            value={formData.expectedCompletionDate}
            onChange={onChange}
            disabled={submitting}
          />
        </div>

        <button
          type="submit"
          className="btn-primary"
          disabled={submitting || triggers.length === 0 || workers.length === 0}
        >
          {submitting ? 'Assigning...' : 'Assign Inspection'}
        </button>
      </form>
      {triggers.length === 0 && (
        <p className="read-only-note">Create at least one business trigger before assigning an inspection.</p>
      )}
      {workers.length === 0 && (
        <p className="read-only-note">No active field employees in your department are available for assignment.</p>
      )}
    </section>
  );
}
