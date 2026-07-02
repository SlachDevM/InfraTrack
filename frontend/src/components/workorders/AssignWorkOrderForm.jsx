import { getOperationalDecisionOutcomeLabel } from '../../constants/operationalDecisionOutcomes';

export default function AssignWorkOrderForm({
  assignFormData,
  createdWorkOrders,
  selectedAssignWorkOrder,
  eligibleAssignees,
  assigning,
  onChange,
  onSubmit,
}) {
  return (
    <section className="work-order-form-section">
      <h2>Assign Work Order</h2>
      <form className="work-order-form" onSubmit={onSubmit}>
        <div className="form-row">
          <label htmlFor="workOrderId">Work Order</label>
          <select
            id="workOrderId"
            name="workOrderId"
            value={assignFormData.workOrderId}
            onChange={onChange}
            required
            disabled={assigning || createdWorkOrders.length === 0}
          >
            <option value="">Select work order</option>
            {createdWorkOrders.map((workOrder) => (
              <option key={workOrder.id} value={workOrder.id}>
                #{workOrder.id} — {workOrder.assetName} (
                {getOperationalDecisionOutcomeLabel(workOrder.workType)})
              </option>
            ))}
          </select>
        </div>

        {selectedAssignWorkOrder && (
          <div className="linked-decision-info">
            <strong>Work Type:</strong>{' '}
            {getOperationalDecisionOutcomeLabel(selectedAssignWorkOrder.workType)}
            <br />
            <strong>Description:</strong> {selectedAssignWorkOrder.description}
          </div>
        )}

        <div className="form-row">
          <label htmlFor="assignedToUserId">Assign To</label>
          <select
            id="assignedToUserId"
            name="assignedToUserId"
            value={assignFormData.assignedToUserId}
            onChange={onChange}
            required
            disabled={assigning || !assignFormData.workOrderId || eligibleAssignees.length === 0}
          >
            <option value="">Select assignee</option>
            {eligibleAssignees.map((worker) => (
              <option key={worker.id} value={worker.id}>
                {worker.name} ({worker.role})
              </option>
            ))}
          </select>
        </div>

        <div className="form-row">
          <label htmlFor="assignedAt">Assignment Date & Time</label>
          <input
            id="assignedAt"
            name="assignedAt"
            type="datetime-local"
            value={assignFormData.assignedAt}
            onChange={onChange}
            required
            disabled={assigning}
          />
        </div>

        <button
          type="submit"
          className="btn-primary"
          disabled={assigning || createdWorkOrders.length === 0 || eligibleAssignees.length === 0}
        >
          {assigning ? 'Assigning...' : 'Assign Work Order'}
        </button>
      </form>
      {createdWorkOrders.length === 0 && (
        <p className="read-only-note">No work orders are awaiting assignment.</p>
      )}
      {assignFormData.workOrderId && eligibleAssignees.length === 0 && (
        <p className="read-only-note">
          No eligible workers are available for this work order type.
        </p>
      )}
    </section>
  );
}
