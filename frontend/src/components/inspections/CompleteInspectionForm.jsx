import { getBusinessTriggerTypeLabel } from '../../constants/businessTriggerTypes';
import { PHYSICAL_CONDITION_OPTIONS } from '../../constants/physicalConditions';

export default function CompleteInspectionForm({
  inspection,
  completeFormData,
  completingId,
  onChange,
  onSubmit,
}) {
  return (
    <form
      className="inspection-form complete-form"
      onSubmit={onSubmit}
    >
      <div className="linked-asset-info">
        <strong>Asset:</strong> {inspection.assetName}
        <br />
        <strong>Trigger:</strong> #{inspection.businessTriggerId} — {getBusinessTriggerTypeLabel(inspection.businessTriggerType)}
        <br />
        <strong>Reason:</strong> {inspection.businessTriggerReason}
      </div>

      <div className="form-row">
        <label htmlFor={`observedCondition-${inspection.id}`}>Observed Condition</label>
        <select
          id={`observedCondition-${inspection.id}`}
          name="observedCondition"
          value={completeFormData.observedCondition}
          onChange={onChange}
          required
          disabled={completingId === inspection.id}
        >
          {PHYSICAL_CONDITION_OPTIONS.map((option) => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>
      </div>

      <div className="form-row">
        <label htmlFor={`observations-${inspection.id}`}>Observations</label>
        <textarea
          id={`observations-${inspection.id}`}
          name="observations"
          value={completeFormData.observations}
          onChange={onChange}
          required
          disabled={completingId === inspection.id}
          rows={3}
        />
      </div>

      <div className="form-row checkbox-row">
        <label htmlFor={`issueIdentified-${inspection.id}`}>
          <input
            id={`issueIdentified-${inspection.id}`}
            name="issueIdentified"
            type="checkbox"
            checked={completeFormData.issueIdentified}
            onChange={onChange}
            disabled={completingId === inspection.id}
          />
          Issue identified (record only — Issue creation is handled separately)
        </label>
      </div>

      <div className="form-row">
        <label htmlFor={`completedAt-${inspection.id}`}>Completion Date & Time</label>
        <input
          id={`completedAt-${inspection.id}`}
          name="completedAt"
          type="datetime-local"
          value={completeFormData.completedAt}
          onChange={onChange}
          required
          disabled={completingId === inspection.id}
        />
      </div>

      <button
        type="submit"
        className="btn-primary"
        disabled={completingId === inspection.id}
      >
        {completingId === inspection.id ? 'Completing...' : 'Complete Inspection'}
      </button>
    </form>
  );
}
