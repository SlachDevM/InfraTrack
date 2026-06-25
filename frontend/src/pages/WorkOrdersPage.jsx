import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import apiClient from '../services/apiClient';
import workOrderApi from '../services/workOrderApi';
import maintenanceActivityApi from '../services/maintenanceActivityApi';
import operationalDecisionApi from '../services/operationalDecisionApi';
import NotificationButton from '../components/NotificationButton';
import { canAssignWorkOrders, canCompleteMaintenance, canCreateWorkOrders, canRecordCompletionReview, USER_ROLES } from '../constants/userRoles';
import { getOperationalDecisionOutcomeLabel } from '../constants/operationalDecisionOutcomes';
import {
  COMPLETION_REVIEW_DECISION_OPTIONS,
  getCompletionReviewDecisionLabel,
} from '../constants/completionReviewDecisions';
import {
  WORK_ORDER_PRIORITIES,
  WORK_ORDER_PRIORITY_OPTIONS,
  getWorkOrderPriorityLabel,
} from '../constants/workOrderPriorities';
import '../styles/ReferenceDataPage.css';
import '../styles/WorkOrdersPage.css';

const PHYSICAL_WORK_OUTCOMES = ['INTERNAL_MAINTENANCE', 'CONTRACTOR_WORK'];

function toDateTimeLocalValue(date = new Date()) {
  const pad = (value) => String(value).padStart(2, '0');
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
}

export default function WorkOrdersPage() {
  const navigate = useNavigate();
  const { auth, logout } = useAuth();
  const [workOrders, setWorkOrders] = useState([]);
  const [maintenanceActivities, setMaintenanceActivities] = useState([]);
  const [decisions, setDecisions] = useState([]);
  const [workers, setWorkers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [assigning, setAssigning] = useState(false);
  const [completing, setCompleting] = useState(false);
  const [reviewing, setReviewing] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [formData, setFormData] = useState({
    operationalDecisionId: '',
    description: '',
    priority: WORK_ORDER_PRIORITIES.NORMAL,
    createdAtBusinessDate: toDateTimeLocalValue(),
  });
  const [assignFormData, setAssignFormData] = useState({
    workOrderId: '',
    assignedToUserId: '',
    assignedAt: toDateTimeLocalValue(),
  });
  const [completeFormData, setCompleteFormData] = useState({
    workOrderId: '',
    completionNotes: '',
    completedAt: toDateTimeLocalValue(),
  });
  const [reviewFormData, setReviewFormData] = useState({
    maintenanceActivityId: '',
    decision: 'APPROVED',
    reviewNotes: '',
    reviewedAt: toDateTimeLocalValue(),
  });

  const canCreate = canCreateWorkOrders(auth?.user?.role);
  const canAssign = canAssignWorkOrders(auth?.user?.role);
  const canComplete = canCompleteMaintenance(auth?.user?.role);
  const canReview = canRecordCompletionReview(auth?.user?.role);
  const currentUserId = auth?.user?.userId;

  const eligibleDecisions = useMemo(() => {
    const decidedIds = new Set(workOrders.map((order) => order.operationalDecisionId));
    return decisions.filter(
      (decision) =>
        PHYSICAL_WORK_OUTCOMES.includes(decision.outcome)
        && !decidedIds.has(decision.id)
    );
  }, [decisions, workOrders]);

  const selectedDecision = useMemo(
    () => decisions.find((decision) => String(decision.id) === String(formData.operationalDecisionId)),
    [decisions, formData.operationalDecisionId]
  );

  const createdWorkOrders = useMemo(
    () => workOrders.filter((order) => order.status === 'CREATED'),
    [workOrders]
  );

  const selectedAssignWorkOrder = useMemo(
    () => workOrders.find((order) => String(order.id) === String(assignFormData.workOrderId)),
    [workOrders, assignFormData.workOrderId]
  );

  const eligibleAssignees = useMemo(() => {
    if (!selectedAssignWorkOrder) {
      return [];
    }
    const requiredRole = selectedAssignWorkOrder.workType === 'INTERNAL_MAINTENANCE'
      ? USER_ROLES.FIELD_EMPLOYEE
      : USER_ROLES.CONTRACTOR;
    return workers.filter((worker) => worker.role === requiredRole);
  }, [selectedAssignWorkOrder, workers]);

  const assignedWorkOrdersForCurrentUser = useMemo(
    () => workOrders.filter(
      (order) => order.status === 'ASSIGNED'
        && currentUserId
        && order.assignedToUserId === currentUserId
    ),
    [workOrders, currentUserId]
  );

  const selectedCompleteWorkOrder = useMemo(
    () => workOrders.find((order) => String(order.id) === String(completeFormData.workOrderId)),
    [workOrders, completeFormData.workOrderId]
  );

  const reviewableMaintenanceActivities = useMemo(
    () => maintenanceActivities.filter(
      (activity) => activity.workOrderStatus === 'COMPLETED'
        && !activity.completionReviewDecision
    ),
    [maintenanceActivities]
  );

  const selectedReviewActivity = useMemo(
    () => maintenanceActivities.find(
      (activity) => String(activity.id) === String(reviewFormData.maintenanceActivityId)
    ),
    [maintenanceActivities, reviewFormData.maintenanceActivityId]
  );

  useEffect(() => {
    if (!auth) {
      navigate('/login');
      return;
    }
    apiClient.setToken(auth.token);
    loadPageData();
  }, [auth, navigate]);

  const loadPageData = async () => {
    try {
      setLoading(true);
      setError(null);
      const [workOrderData, decisionData, workerData, maintenanceActivityData] = await Promise.all([
        workOrderApi.list(),
        operationalDecisionApi.list(),
        canAssign ? workOrderApi.listWorkers() : Promise.resolve([]),
        maintenanceActivityApi.list(),
      ]);
      setWorkOrders(workOrderData);
      setDecisions(decisionData);
      setWorkers(workerData);
      setMaintenanceActivities(maintenanceActivityData);
    } catch (err) {
      setError(`Failed to load work orders: ${err.message}`);
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!canCreate) return;

    try {
      setSubmitting(true);
      setError(null);
      setSuccess(null);
      await workOrderApi.create({
        operationalDecisionId: Number(formData.operationalDecisionId),
        description: formData.description,
        priority: formData.priority,
        createdAtBusinessDate: `${formData.createdAtBusinessDate}:00`,
      });
      setSuccess('Work order created successfully.');
      setFormData({
        operationalDecisionId: '',
        description: '',
        priority: WORK_ORDER_PRIORITIES.NORMAL,
        createdAtBusinessDate: toDateTimeLocalValue(),
      });
      await loadPageData();
    } catch (err) {
      if (err.status === 403) {
        setError('You do not have permission to create work orders.');
      } else {
        setError(`Failed to create work order: ${err.message}`);
      }
    } finally {
      setSubmitting(false);
    }
  };

  const handleAssignChange = (e) => {
    const { name, value } = e.target;
    setAssignFormData((prev) => ({
      ...prev,
      [name]: value,
      ...(name === 'workOrderId' ? { assignedToUserId: '' } : {}),
    }));
  };

  const handleAssignSubmit = async (e) => {
    e.preventDefault();
    if (!canAssign) return;

    try {
      setAssigning(true);
      setError(null);
      setSuccess(null);
      await workOrderApi.assign(Number(assignFormData.workOrderId), {
        assignedToUserId: Number(assignFormData.assignedToUserId),
        assignedAt: `${assignFormData.assignedAt}:00`,
      });
      setSuccess('Work order assigned successfully.');
      setAssignFormData({
        workOrderId: '',
        assignedToUserId: '',
        assignedAt: toDateTimeLocalValue(),
      });
      await loadPageData();
    } catch (err) {
      if (err.status === 403) {
        setError('You do not have permission to assign work orders.');
      } else {
        setError(`Failed to assign work order: ${err.message}`);
      }
    } finally {
      setAssigning(false);
    }
  };

  const handleCompleteChange = (e) => {
    const { name, value } = e.target;
    setCompleteFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleCompleteSubmit = async (e) => {
    e.preventDefault();
    if (!canComplete) return;

    try {
      setCompleting(true);
      setError(null);
      setSuccess(null);
      await workOrderApi.completeMaintenance(Number(completeFormData.workOrderId), {
        completionNotes: completeFormData.completionNotes,
        completedAt: `${completeFormData.completedAt}:00`,
      });
      setSuccess('Maintenance activity completed successfully.');
      setCompleteFormData({
        workOrderId: '',
        completionNotes: '',
        completedAt: toDateTimeLocalValue(),
      });
      await loadPageData();
    } catch (err) {
      if (err.status === 403) {
        setError('You do not have permission to complete maintenance for this work order.');
      } else {
        setError(`Failed to complete maintenance: ${err.message}`);
      }
    } finally {
      setCompleting(false);
    }
  };

  const handleReviewChange = (e) => {
    const { name, value } = e.target;
    setReviewFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleReviewSubmit = async (e) => {
    e.preventDefault();
    if (!canReview) return;

    try {
      setReviewing(true);
      setError(null);
      setSuccess(null);
      await maintenanceActivityApi.recordCompletionReview(
        Number(reviewFormData.maintenanceActivityId),
        {
          decision: reviewFormData.decision,
          reviewNotes: reviewFormData.reviewNotes,
          reviewedAt: `${reviewFormData.reviewedAt}:00`,
        }
      );
      setSuccess('Completion review recorded successfully.');
      setReviewFormData({
        maintenanceActivityId: '',
        decision: 'APPROVED',
        reviewNotes: '',
        reviewedAt: toDateTimeLocalValue(),
      });
      await loadPageData();
    } catch (err) {
      if (err.status === 403) {
        setError('You do not have permission to record completion reviews.');
      } else {
        setError(`Failed to record completion review: ${err.message}`);
      }
    } finally {
      setReviewing(false);
    }
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  if (loading) {
    return <div className="loading">Loading work orders...</div>;
  }

  return (
    <div className="reference-data-page work-orders-page">
      <header
        className="reference-header"
        style={{
          background: 'linear-gradient(135deg, #1a472a 0%, #2d6b4d 100%)',
          color: 'white',
        }}
      >
        <button type="button" className="back-btn" onClick={() => navigate('/')}>
          ← Back
        </button>
        <h1>Work Orders</h1>
        <div className="user-header-actions">
          <NotificationButton />
          <button type="button" className="logout-btn" onClick={handleLogout}>
            Logout
          </button>
        </div>
      </header>

      <main className="reference-content work-orders-content">
        {error && <div className="error-message">{error}</div>}
        {success && <div className="success-message">{success}</div>}

        {canCreate ? (
          <section className="work-order-form-section">
            <h2>Create Work Order</h2>
            <form className="work-order-form" onSubmit={handleSubmit}>
              <div className="form-row">
                <label htmlFor="operationalDecisionId">Operational Decision</label>
                <select
                  id="operationalDecisionId"
                  name="operationalDecisionId"
                  value={formData.operationalDecisionId}
                  onChange={handleChange}
                  required
                  disabled={submitting || eligibleDecisions.length === 0}
                >
                  <option value="">Select operational decision</option>
                  {eligibleDecisions.map((decision) => (
                    <option key={decision.id} value={decision.id}>
                      #{decision.id} — {decision.assetName} ({getOperationalDecisionOutcomeLabel(decision.outcome)})
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
                  onChange={handleChange}
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
                  onChange={handleChange}
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
                  onChange={handleChange}
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
        ) : (
          <p className="read-only-note">
            Work order creation is available to Operational Coordinators.
          </p>
        )}

        {canAssign ? (
          <section className="work-order-form-section">
            <h2>Assign Work Order</h2>
            <form className="work-order-form" onSubmit={handleAssignSubmit}>
              <div className="form-row">
                <label htmlFor="workOrderId">Work Order</label>
                <select
                  id="workOrderId"
                  name="workOrderId"
                  value={assignFormData.workOrderId}
                  onChange={handleAssignChange}
                  required
                  disabled={assigning || createdWorkOrders.length === 0}
                >
                  <option value="">Select work order</option>
                  {createdWorkOrders.map((workOrder) => (
                    <option key={workOrder.id} value={workOrder.id}>
                      #{workOrder.id} — {workOrder.assetName} ({getOperationalDecisionOutcomeLabel(workOrder.workType)})
                    </option>
                  ))}
                </select>
              </div>

              {selectedAssignWorkOrder && (
                <div className="linked-decision-info">
                  <strong>Work Type:</strong> {getOperationalDecisionOutcomeLabel(selectedAssignWorkOrder.workType)}
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
                  onChange={handleAssignChange}
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
                  onChange={handleAssignChange}
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
        ) : (
          <p className="read-only-note">
            Work order assignment is available to Operational Coordinators.
          </p>
        )}

        {canComplete ? (
          <section className="work-order-form-section">
            <h2>Complete Maintenance Activity</h2>
            <form className="work-order-form" onSubmit={handleCompleteSubmit}>
              <div className="form-row">
                <label htmlFor="completeWorkOrderId">Assigned Work Order</label>
                <select
                  id="completeWorkOrderId"
                  name="workOrderId"
                  value={completeFormData.workOrderId}
                  onChange={handleCompleteChange}
                  required
                  disabled={completing || assignedWorkOrdersForCurrentUser.length === 0}
                >
                  <option value="">Select assigned work order</option>
                  {assignedWorkOrdersForCurrentUser.map((workOrder) => (
                    <option key={workOrder.id} value={workOrder.id}>
                      #{workOrder.id} — {workOrder.assetName} ({getOperationalDecisionOutcomeLabel(workOrder.workType)})
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
                  onChange={handleCompleteChange}
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
                  onChange={handleCompleteChange}
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
        ) : (
          <p className="read-only-note">
            Maintenance completion is available to assigned Field Employees and Contractors.
          </p>
        )}

        {canReview ? (
          <section className="work-order-form-section">
            <h2>Record Completion Review</h2>
            <form className="work-order-form" onSubmit={handleReviewSubmit}>
              <div className="form-row">
                <label htmlFor="maintenanceActivityId">Maintenance Activity</label>
                <select
                  id="maintenanceActivityId"
                  name="maintenanceActivityId"
                  value={reviewFormData.maintenanceActivityId}
                  onChange={handleReviewChange}
                  required
                  disabled={reviewing || reviewableMaintenanceActivities.length === 0}
                >
                  <option value="">Select maintenance activity</option>
                  {reviewableMaintenanceActivities.map((activity) => (
                    <option key={activity.id} value={activity.id}>
                      #{activity.id} — WO #{activity.workOrderId} — {activity.assetName}
                    </option>
                  ))}
                </select>
              </div>

              {selectedReviewActivity && (
                <div className="linked-decision-info">
                  <strong>Asset:</strong> {selectedReviewActivity.assetName}
                  <br />
                  <strong>Completion Notes:</strong> {selectedReviewActivity.completionNotes}
                  <br />
                  <strong>Completed:</strong>{' '}
                  {selectedReviewActivity.completedAt
                    ? new Date(selectedReviewActivity.completedAt).toLocaleString()
                    : '-'}
                </div>
              )}

              <div className="form-row">
                <label htmlFor="decision">Review Decision</label>
                <select
                  id="decision"
                  name="decision"
                  value={reviewFormData.decision}
                  onChange={handleReviewChange}
                  required
                  disabled={reviewing}
                >
                  {COMPLETION_REVIEW_DECISION_OPTIONS.map((option) => (
                    <option key={option.value} value={option.value}>
                      {option.label}
                    </option>
                  ))}
                </select>
              </div>

              <div className="form-row">
                <label htmlFor="reviewNotes">Review Notes</label>
                <textarea
                  id="reviewNotes"
                  name="reviewNotes"
                  value={reviewFormData.reviewNotes}
                  onChange={handleReviewChange}
                  required
                  disabled={reviewing}
                  rows={3}
                />
              </div>

              <div className="form-row">
                <label htmlFor="reviewedAt">Review Date & Time</label>
                <input
                  id="reviewedAt"
                  name="reviewedAt"
                  type="datetime-local"
                  value={reviewFormData.reviewedAt}
                  onChange={handleReviewChange}
                  required
                  disabled={reviewing}
                />
              </div>

              <button
                type="submit"
                className="btn-primary"
                disabled={reviewing || reviewableMaintenanceActivities.length === 0}
              >
                {reviewing ? 'Recording...' : 'Record Completion Review'}
              </button>
            </form>
            {reviewableMaintenanceActivities.length === 0 && (
              <p className="read-only-note">
                No completed maintenance activities are awaiting completion review.
              </p>
            )}
          </section>
        ) : (
          <p className="read-only-note">
            Completion review is available to Managers.
          </p>
        )}

        <section className="work-order-list-section">
          <h2>Work Orders</h2>
          {workOrders.length === 0 ? (
            <p className="no-items">No work orders yet.</p>
          ) : (
            <table className="reference-table work-orders-table">
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
                        : activity ? 'Pending' : '-'}
                    </td>
                    <td>
                      {workOrder.createdAtBusinessDate
                        ? new Date(workOrder.createdAtBusinessDate).toLocaleString()
                        : '-'}
                    </td>
                    <td>
                      {workOrder.assignedAt
                        ? new Date(workOrder.assignedAt).toLocaleString()
                        : '-'}
                    </td>
                  </tr>
                  );
                })}
              </tbody>
            </table>
          )}
        </section>
      </main>
    </div>
  );
}
