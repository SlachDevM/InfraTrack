import { getOperationalDecisionOutcomeLabel } from '../../constants/operationalDecisionOutcomes';
import { getWorkOrderPriorityLabel } from '../../constants/workOrderPriorities';
import { getWorkOrderStatusLabel } from '../../constants/workOrderStatuses';
import { getCompletionReviewDecisionLabel } from '../../constants/completionReviewDecisions';
import { COMMON_MESSAGES } from '../../constants/messages';

function formatAssignedUser(name) {
  return name?.trim() ? name.trim() : 'Unassigned';
}

function formatDateTime(value) {
  return value ? new Date(value).toLocaleString() : '—';
}

export default function WorkOrderList({ workOrders, maintenanceActivities }) {
  return (
    <section className="work-order-list-section" aria-labelledby="work-orders-heading">
      <h2 id="work-orders-heading">Work Orders</h2>
      {workOrders.length === 0 ? (
        <p className="empty-state no-items" role="status">
          {COMMON_MESSAGES.NO_WORK_ORDERS}
        </p>
      ) : (
        <div className="table-scroll">
          <table className="reference-table work-orders-table" aria-label="Work orders">
            <thead>
              <tr>
                <th scope="col">Asset</th>
                <th scope="col">Decision</th>
                <th scope="col">Work Type</th>
                <th scope="col">Description</th>
                <th scope="col">Priority</th>
                <th scope="col">Status</th>
                <th scope="col">Assigned To</th>
                <th scope="col">Review</th>
                <th scope="col">Created At</th>
                <th scope="col">Assigned At</th>
              </tr>
            </thead>
            <tbody>
              {workOrders.map((workOrder) => {
                const activity = maintenanceActivities.find(
                  (item) => item.workOrderId === workOrder.id
                );
                const reviewLabel = activity?.completionReviewDecision
                  ? getCompletionReviewDecisionLabel(activity.completionReviewDecision)
                  : activity
                    ? 'Pending review'
                    : '—';

                return (
                  <tr key={workOrder.id}>
                    <td>{workOrder.assetName}</td>
                    <td>#{workOrder.operationalDecisionId}</td>
                    <td>{getOperationalDecisionOutcomeLabel(workOrder.workType)}</td>
                    <td>{workOrder.description}</td>
                    <td>{getWorkOrderPriorityLabel(workOrder.priority)}</td>
                    <td>
                      <span className={`status-badge status-${workOrder.status?.toLowerCase()}`}>
                        {getWorkOrderStatusLabel(workOrder.status)}
                      </span>
                    </td>
                    <td>{formatAssignedUser(workOrder.assignedToUserName)}</td>
                    <td>{reviewLabel}</td>
                    <td>{formatDateTime(workOrder.createdAtBusinessDate)}</td>
                    <td>{formatDateTime(workOrder.assignedAt)}</td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}
    </section>
  );
}
