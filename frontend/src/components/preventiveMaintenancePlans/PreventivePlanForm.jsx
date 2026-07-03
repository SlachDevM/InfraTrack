import { PREVENTIVE_MAINTENANCE_PLAN_STATUS_OPTIONS } from '../../constants/preventiveMaintenancePlanStatuses';
import { PREVENTIVE_MAINTENANCE_PLAN_PRIORITY_OPTIONS } from '../../constants/preventiveMaintenancePlanPriorities';
import { PLAN_TRIGGER_TYPE_OPTIONS } from '../../constants/planTriggerTypes';
import { PLAN_TARGET_ACTION_OPTIONS } from '../../constants/planTargetActions';
import PreventivePlanTriggerFields from './PreventivePlanTriggerFields';

export default function PreventivePlanForm({
  editingId,
  formData,
  submitting,
  assets,
  templates,
  triggerSummaryPreview,
  onChange,
  onSubmit,
  onCancelEdit,
}) {
  return (
    <section className="reference-form-section">
      <h2>
        {editingId ? 'Edit Preventive Maintenance Plan' : 'Create Preventive Maintenance Plan'}
      </h2>
      <form className="reference-form reference-form-stacked" onSubmit={onSubmit}>
        <div className="reference-form-grid">
          <div className="form-row">
            <label htmlFor="planCode">Plan Code</label>
            <input
              id="planCode"
              name="planCode"
              type="text"
              value={formData.planCode}
              onChange={onChange}
              required={!editingId}
              disabled={submitting || Boolean(editingId)}
              placeholder="PUMP_MONTHLY"
            />
          </div>
          <div className="form-row">
            <label htmlFor="version">Version</label>
            <input
              id="version"
              name="version"
              type="number"
              min="1"
              value={formData.version}
              onChange={onChange}
              required
              disabled={submitting}
            />
          </div>
          <div className="form-row">
            <label htmlFor="name">Name</label>
            <input
              id="name"
              name="name"
              type="text"
              value={formData.name}
              onChange={onChange}
              required
              disabled={submitting}
            />
          </div>
        </div>

        <div className="form-row form-row-full">
          <label htmlFor="description">Description</label>
          <textarea
            id="description"
            name="description"
            value={formData.description}
            onChange={onChange}
            disabled={submitting}
            rows={3}
          />
        </div>

        <div className="reference-form-grid">
          <div className="form-row">
            <label htmlFor="assetId">Asset</label>
            <select
              id="assetId"
              name="assetId"
              value={formData.assetId}
              onChange={onChange}
              required={!editingId}
              disabled={submitting || Boolean(editingId)}
            >
              <option value="">Select asset</option>
              {assets.map((asset) => (
                <option key={asset.id} value={asset.id}>
                  {asset.name}
                </option>
              ))}
            </select>
          </div>
          <div className="form-row">
            <label htmlFor="status">Status</label>
            <select
              id="status"
              name="status"
              value={formData.status}
              onChange={onChange}
              required
              disabled={submitting}
            >
              {PREVENTIVE_MAINTENANCE_PLAN_STATUS_OPTIONS.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
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
              {PREVENTIVE_MAINTENANCE_PLAN_PRIORITY_OPTIONS.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </div>
          <div className="form-row">
            <label htmlFor="targetAction">Target Action</label>
            <select
              id="targetAction"
              name="targetAction"
              value={formData.targetAction}
              onChange={onChange}
              required
              disabled={submitting}
            >
              {PLAN_TARGET_ACTION_OPTIONS.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </div>
        </div>

        <div className="reference-form-grid">
          <div className="form-row">
            <label htmlFor="triggerType">Trigger Type</label>
            <select
              id="triggerType"
              name="triggerType"
              value={formData.triggerType}
              onChange={onChange}
              required
              disabled={submitting}
            >
              {PLAN_TRIGGER_TYPE_OPTIONS.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </div>
          <PreventivePlanTriggerFields
            triggerType={formData.triggerType}
            formData={formData}
            submitting={submitting}
            onChange={onChange}
          />
        </div>

        {formData.targetAction === 'CREATE_INSPECTION' && (
          <div className="form-row form-row-full">
            <label htmlFor="inspectionTemplateId">Inspection Template (optional)</label>
            <select
              id="inspectionTemplateId"
              name="inspectionTemplateId"
              value={formData.inspectionTemplateId}
              onChange={onChange}
              disabled={submitting}
            >
              <option value="">None</option>
              {templates.map((template) => (
                <option key={template.id} value={template.id}>
                  {template.name}
                </option>
              ))}
            </select>
          </div>
        )}

        {triggerSummaryPreview && (
          <p className="read-only-note">
            Trigger summary: <strong>{triggerSummaryPreview}</strong>
          </p>
        )}
        <div className="form-row">
          <label htmlFor="triggerActive">
            <input
              id="triggerActive"
              name="triggerActive"
              type="checkbox"
              checked={formData.triggerActive}
              onChange={onChange}
              disabled={submitting}
            />{' '}
            Trigger active
          </label>
        </div>
        <div className="form-actions">
          <button type="submit" className="btn-primary" disabled={submitting}>
            {submitting ? 'Saving...' : editingId ? 'Update Plan' : 'Create Plan'}
          </button>
          {editingId && (
            <button
              type="button"
              className="btn-secondary"
              onClick={onCancelEdit}
              disabled={submitting}
            >
              Cancel Edit
            </button>
          )}
        </div>
      </form>
    </section>
  );
}
