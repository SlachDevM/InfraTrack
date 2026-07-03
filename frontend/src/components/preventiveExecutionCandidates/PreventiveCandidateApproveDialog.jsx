import { Link } from 'react-router-dom';
import { ROUTES } from '../../constants/routes';

export default function PreventiveCandidateApproveDialog({
  approveCandidate,
  approveForm,
  workers,
  reviewing,
  selectedAssigneeId,
  createdInspectionId,
  onSubmit,
  onCancel,
  onFormChange,
}) {
  return (
    <section className="reference-form-section dialog-panel">
      <h2>Approve Candidate</h2>
      <p>
        Plan: <strong>{approveCandidate.planCodeSnapshot}</strong> —{approveCandidate.assetName}
      </p>
      <p>{approveCandidate.eligibilityReason}</p>
      <form onSubmit={onSubmit}>
        <label htmlFor="approveAssigneeId">
          Assignee
          <select
            id="approveAssigneeId"
            value={approveForm.assigneeId}
            onChange={(e) => onFormChange('assigneeId', e.target.value)}
            required
          >
            <option value="">Select field employee</option>
            {workers.map((worker) => (
              <option key={worker.id} value={worker.id}>
                {worker.name}
              </option>
            ))}
          </select>
        </label>
        <label htmlFor="approvePlannedAt">
          Planned Date
          <input
            id="approvePlannedAt"
            type="date"
            value={approveForm.plannedAt}
            onChange={(e) => onFormChange('plannedAt', e.target.value)}
          />
        </label>
        <label htmlFor="approveNotes">
          Notes
          <textarea
            id="approveNotes"
            value={approveForm.notes}
            onChange={(e) => onFormChange('notes', e.target.value)}
            rows={3}
          />
        </label>
        <div className="form-actions">
          <button
            type="submit"
            className="btn-primary"
            disabled={reviewing || selectedAssigneeId == null}
          >
            {reviewing ? 'Approving...' : 'Approve and Create Inspection'}
          </button>{' '}
          <button type="button" className="btn-secondary" onClick={onCancel}>
            Cancel
          </button>
        </div>
      </form>
      {createdInspectionId && (
        <p>
          Inspection created: <Link to={ROUTES.INSPECTIONS}>#{createdInspectionId}</Link>
        </p>
      )}
    </section>
  );
}
