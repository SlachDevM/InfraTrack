import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import apiClient from '../services/apiClient';
import workOrderApi from '../services/workOrderApi';
import maintenanceActivityApi from '../services/maintenanceActivityApi';
import operationalDecisionApi from '../services/operationalDecisionApi';
import NotificationButton from '../components/NotificationButton';
import PaginationControls from '../components/PaginationControls';
import CreateWorkOrderForm from '../components/workorders/CreateWorkOrderForm';
import AssignWorkOrderForm from '../components/workorders/AssignWorkOrderForm';
import CompleteMaintenanceForm from '../components/workorders/CompleteMaintenanceForm';
import WorkOrderList from '../components/workorders/WorkOrderList';
import { canAssignWorkOrders, canCompleteMaintenance, canCreateWorkOrders, canRecordCompletionReview, USER_ROLES } from '../constants/userRoles';
import {
  COMPLETION_REVIEW_DECISION_OPTIONS,
} from '../constants/completionReviewDecisions';
import {
  WORK_ORDER_PRIORITIES,
} from '../constants/workOrderPriorities';
import { getApiErrorMessage, isForbidden } from '../utils/apiError';
import {
  DEFAULT_PAGE,
  MAX_PAGE_SIZE,
  getPageNumber,
  getTotalPages,
  unwrapPageContent,
} from '../utils/pagination';
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
  const [workOrdersPage, setWorkOrdersPage] = useState(DEFAULT_PAGE);
  const [workOrdersTotalPages, setWorkOrdersTotalPages] = useState(0);
  const [listLoading, setListLoading] = useState(false);
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

  const loadWorkOrders = async (page = workOrdersPage) => {
    try {
      setListLoading(true);
      const workOrderPage = await workOrderApi.list(page);
      setWorkOrders(unwrapPageContent(workOrderPage));
      setWorkOrdersPage(getPageNumber(workOrderPage, page));
      setWorkOrdersTotalPages(getTotalPages(workOrderPage));
    } catch (err) {
      setError(getApiErrorMessage(err, 'Failed to load work orders.'));
    } finally {
      setListLoading(false);
    }
  };

  const loadPageData = async (page = workOrdersPage) => {
    try {
      setLoading(true);
      setError(null);
      const [workOrderPage, decisionData, workerData, maintenanceActivityData] = await Promise.all([
        workOrderApi.list(page),
        operationalDecisionApi.list(0, MAX_PAGE_SIZE),
        canAssign ? workOrderApi.listWorkers() : Promise.resolve([]),
        maintenanceActivityApi.list(),
      ]);
      setWorkOrders(unwrapPageContent(workOrderPage));
      setWorkOrdersPage(getPageNumber(workOrderPage, page));
      setWorkOrdersTotalPages(getTotalPages(workOrderPage));
      setDecisions(unwrapPageContent(decisionData));
      setWorkers(workerData);
      setMaintenanceActivities(maintenanceActivityData);
    } catch (err) {
      setError(getApiErrorMessage(err, 'Failed to load work orders.'));
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
      await loadPageData(workOrdersPage);
    } catch (err) {
      if (isForbidden(err)) {
        setError('You do not have permission to create work orders.');
      } else {
        setError(getApiErrorMessage(err, 'Failed to create work order.'));
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
      await loadPageData(workOrdersPage);
    } catch (err) {
      if (isForbidden(err)) {
        setError('You do not have permission to assign work orders.');
      } else {
        setError(getApiErrorMessage(err, 'Failed to assign work order.'));
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
      await loadPageData(workOrdersPage);
    } catch (err) {
      if (isForbidden(err)) {
        setError('You do not have permission to complete maintenance for this work order.');
      } else {
        setError(getApiErrorMessage(err, 'Failed to complete maintenance.'));
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
      await loadPageData(workOrdersPage);
    } catch (err) {
      if (isForbidden(err)) {
        setError('You do not have permission to record completion reviews.');
      } else {
        setError(getApiErrorMessage(err, 'Failed to record completion review.'));
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
          <CreateWorkOrderForm
            formData={formData}
            eligibleDecisions={eligibleDecisions}
            selectedDecision={selectedDecision}
            submitting={submitting}
            onChange={handleChange}
            onSubmit={handleSubmit}
          />
        ) : (
          <p className="read-only-note">
            Work order creation is available to Operational Coordinators.
          </p>
        )}

        {canAssign ? (
          <AssignWorkOrderForm
            assignFormData={assignFormData}
            createdWorkOrders={createdWorkOrders}
            selectedAssignWorkOrder={selectedAssignWorkOrder}
            eligibleAssignees={eligibleAssignees}
            assigning={assigning}
            onChange={handleAssignChange}
            onSubmit={handleAssignSubmit}
          />
        ) : (
          <p className="read-only-note">
            Work order assignment is available to Operational Coordinators.
          </p>
        )}

        {canComplete ? (
          <CompleteMaintenanceForm
            completeFormData={completeFormData}
            assignedWorkOrdersForCurrentUser={assignedWorkOrdersForCurrentUser}
            selectedCompleteWorkOrder={selectedCompleteWorkOrder}
            completing={completing}
            onChange={handleCompleteChange}
            onSubmit={handleCompleteSubmit}
          />
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

        <WorkOrderList
          workOrders={workOrders}
          maintenanceActivities={maintenanceActivities}
        />
        <PaginationControls
          page={workOrdersPage}
          totalPages={workOrdersTotalPages}
          loading={listLoading}
          onPrevious={() => loadWorkOrders(workOrdersPage - 1)}
          onNext={() => loadWorkOrders(workOrdersPage + 1)}
        />
      </main>
    </div>
  );
}
