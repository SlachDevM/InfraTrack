import { getOperationalDecisionOutcomeLabel } from '../../constants/operationalDecisionOutcomes';
import { getWorkOrderPriorityLabel } from '../../constants/workOrderPriorities';
import { getCompletionReviewDecisionLabel } from '../../constants/completionReviewDecisions';
import { COMMON_MESSAGES } from '../../constants/messages';

export default function WorkOrderList({ workOrders, maintenanceActivities }) {
  return (
    <section className="work-order-list-section">
      <h2>Work Orders</h2>
      {workOrders.length === 0 ? (
        <p className="empty-state no-items">{COMMON_MESSAGES.NO_WORK_ORDERS}</p>
      ) : (
        <div className="table-scroll">
          <table className="reference-table work-orders-table" aria-label="Work orders">
            <thead>
              <tr>
                <th>Asset</th>
                <th>Decision</th>
                <th>Work Type</th>
                <th>Description</th>
                <th>Priority</th>
                <th>Status</th>
                <th>Assigned To</th>
                <th>Review</th>
                <th>Created</th>
                <th>Assigned</th>
              </tr>
            </thead>
            <tbody>
              {workOrders.map((workOrder) => {
                const activity = maintenanceActivities.find(
                  (item) => item.workOrderId === workOrder.id
                );
                return (
                  <tr key={workOrder.id}>
                    <td>{workOrder.assetName}</td>
                    <td>#{workOrder.operationalDecisionId}</td>
                    <td>{getOperationalDecisionOutcomeLabel(workOrder.workType)}</td>
                    <td>{workOrder.description}</td>
                    <td>{getWorkOrderPriorityLabel(workOrder.priority)}</td>
                    <td>{workOrder.status}</td>
                    <td>{workOrder.assignedToUserName || '-'}</td>
                    <td>
                      {activity?.completionReviewDecision
                        ? getCompletionReviewDecisionLabel(activity.completionReviewDecision)
                        : activity
                          ? 'Pending'
                          : '-'}
                    </td>
                    <td>
                      {workOrder.createdAtBusinessDate
                        ? new Date(workOrder.createdAtBusinessDate).toLocaleString()
                        : '-'}
                    </td>
                    <td>
                      {workOrder.assignedAt ? new Date(workOrder.assignedAt).toLocaleString() : '-'}
                    </td>
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
