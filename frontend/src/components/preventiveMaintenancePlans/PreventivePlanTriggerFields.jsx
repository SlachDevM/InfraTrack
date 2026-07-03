import { PLAN_TIME_UNIT_OPTIONS } from '../../constants/planTimeUnits';
import { PLAN_METER_TYPE_OPTIONS } from '../../constants/planMeterTypes';
import { PLAN_EVENT_TYPE_OPTIONS } from '../../constants/planEventTypes';

export default function PreventivePlanTriggerFields({
  triggerType,
  formData,
  submitting,
  onChange,
}) {
  if (triggerType === 'TIME') {
    return (
      <>
        <div className="form-row">
          <label htmlFor="timeEvery">Every</label>
          <input
            id="timeEvery"
            name="timeEvery"
            type="number"
            min="1"
            value={formData.timeEvery}
            onChange={onChange}
            required
            disabled={submitting}
          />
        </div>
        <div className="form-row">
          <label htmlFor="timeUnit">Unit</label>
          <select
            id="timeUnit"
            name="timeUnit"
            value={formData.timeUnit}
            onChange={onChange}
            required
            disabled={submitting}
          >
            {PLAN_TIME_UNIT_OPTIONS.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </div>
      </>
    );
  }

  if (triggerType === 'METER') {
    return (
      <>
        <div className="form-row">
          <label htmlFor="meterType">Meter</label>
          <select
            id="meterType"
            name="meterType"
            value={formData.meterType}
            onChange={onChange}
            required
            disabled={submitting}
          >
            {PLAN_METER_TYPE_OPTIONS.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </div>
        <div className="form-row">
          <label htmlFor="meterEvery">Every</label>
          <input
            id="meterEvery"
            name="meterEvery"
            type="number"
            min="1"
            value={formData.meterEvery}
            onChange={onChange}
            required
            disabled={submitting}
          />
        </div>
      </>
    );
  }

  if (triggerType === 'EVENT') {
    return (
      <div className="form-row">
        <label htmlFor="eventType">Event</label>
        <select
          id="eventType"
          name="eventType"
          value={formData.eventType}
          onChange={onChange}
          required
          disabled={submitting}
        >
          {PLAN_EVENT_TYPE_OPTIONS.map((option) => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>
      </div>
    );
  }

  return null;
}
