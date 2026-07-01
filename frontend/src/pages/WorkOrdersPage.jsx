import { useEffect, useMemo, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
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
import ExportCsvButton from '../components/ExportCsvButton';
import { WORK_ORDER_STATUS } from '../constants/statuses';
import { REPORTING_EXPORT_TYPES } from '../constants/reportingExports';
import { ROUTES } from '../constants/routes';
import { canAssignWorkOrders, canCompleteMaintenance, canCreateWorkOrders, canRecordCompletionReview, canExportReporting, USER_ROLES } from '../constants/userRoles';
import {
  COMPLETION_REVIEW_DECISION_OPTIONS,
} from '../constants/completionReviewDecisions';
import { ISSUE_SEVERITY_OPTIONS } from '../constants/issueSeverities';
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
  const [reviewableMaintenanceActivities, setReviewableMaintenanceActivities] = useState([]);
  const [decisions, setDecisions] = useState([]);
  const [assignableWorkOrders, setAssignableWorkOrders] = useState([]);
  const [eligibleAssignees, setEligibleAssignees] = useState([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [assigning, setAssigning] = useState(false);
  const [completing, setCompleting] = useState(false);
  const [reviewing, setReviewing] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [showReworkDecisionLink, setShowReworkDecisionLink] = useState(false);
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
    reworkSeverity: 'MEDIUM',
    rootCause: '',
    correctiveAction: '',
    preventiveAction: '',
  });

  const canCreate = canCreateWorkOrders(auth?.user?.role);
  const canAssign = canAssignWorkOrders(auth?.user?.role);
  const canComplete = canCompleteMaintenance(auth?.user?.role);
  const canReview = canRecordCompletionReview(auth?.user?.role);
  const canExport = canExportReporting(auth?.user?.role);
  const isReworkRequired = reviewFormData.decision === 'REWORK_REQUIRED';
  const currentUserId = auth?.user?.userId;

  const selectedDecision = useMemo(
    () => decisions.find((decision) => String(decision.id) === String(formData.operationalDecisionId)),
    [decisions, formData.operationalDecisionId]
  );

  const selectedAssignWorkOrder = useMemo(
    () => assignableWorkOrders.find((order) => String(order.id) === String(assignFormData.workOrderId)),
    [assignableWorkOrders, assignFormData.workOrderId]
  );

  useEffect(() => {
    if (!selectedAssignWorkOrder?.assetDepartmentId) {
      setEligibleAssignees([]);
      return;
    }
    const requiredRole = selectedAssignWorkOrder.workType === 'INTERNAL_MAINTENANCE'
      ? USER_ROLES.FIELD_EMPLOYEE
      : USER_ROLES.CONTRACTOR;
    workOrderApi.listEligibleWorkers(selectedAssignWorkOrder.assetDepartmentId, requiredRole)
      .then(setEligibleAssignees)
      .catch((err) => {
        setEligibleAssignees([]);
        setError(getApiErrorMessage(err, 'Failed to load eligible assignees.'));
      });
  }, [selectedAssignWorkOrder]);

  const assignedWorkOrdersForCurrentUser = useMemo(
    () => workOrders.filter(
      (order) => order.status === WORK_ORDER_STATUS.ASSIGNED
        && currentUserId
        && order.assignedToUserId === currentUserId
    ),
    [workOrders, currentUserId]
  );

  const selectedCompleteWorkOrder = useMemo(
    () => workOrders.find((order) => String(order.id) === String(completeFormData.workOrderId)),
    [workOrders, completeFormData.workOrderId]
  );

  const selectedReviewActivity = useMemo(
    () => reviewableMaintenanceActivities.find(
      (activity) => String(activity.id) === String(reviewFormData.maintenanceActivityId)
    ),
    [reviewableMaintenanceActivities, reviewFormData.maintenanceActivityId]
  );

  useEffect(() => {
    if (!auth) {
      navigate(ROUTES.LOGIN);
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
      const [workOrderPage, decisionData, maintenanceActivityData, assignablePage, reviewableActivityData] = await Promise.all([
        workOrderApi.list(page),
        canCreate
          ? operationalDecisionApi.listEligibleForWorkOrderCreation(DEFAULT_PAGE, MAX_PAGE_SIZE)
          : Promise.resolve(null),
        canComplete ? maintenanceActivityApi.list() : Promise.resolve([]),
        canAssign
          ? workOrderApi.listEligibleForAssignment(DEFAULT_PAGE, MAX_PAGE_SIZE)
          : Promise.resolve(null),
        canReview
          ? maintenanceActivityApi.listEligibleForCompletionReview()
          : Promise.resolve([]),
      ]);
      setWorkOrders(unwrapPageContent(workOrderPage));
      setWorkOrdersPage(getPageNumber(workOrderPage, page));
      setWorkOrdersTotalPages(getTotalPages(workOrderPage));
      setDecisions(decisionData ? unwrapPageContent(decisionData) : []);
      setAssignableWorkOrders(assignablePage ? unwrapPageContent(assignablePage) : []);
      setMaintenanceActivities(maintenanceActivityData);
      setReviewableMaintenanceActivities(reviewableActivityData);
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
      setShowReworkDecisionLink(false);
      const payload = {
        decision: reviewFormData.decision,
        reviewNotes: reviewFormData.reviewNotes,
        reviewedAt: `${reviewFormData.reviewedAt}:00`,
      };
      if (reviewFormData.decision === 'REWORK_REQUIRED') {
        payload.reworkSeverity = reviewFormData.reworkSeverity;
        if (reviewFormData.rootCause.trim()) {
          payload.rootCause = reviewFormData.rootCause.trim();
        }
        if (reviewFormData.correctiveAction.trim()) {
          payload.correctiveAction = reviewFormData.correctiveAction.trim();
        }
        if (reviewFormData.preventiveAction.trim()) {
          payload.preventiveAction = reviewFormData.preventiveAction.trim();
        }
      }
      const response = await maintenanceActivityApi.recordCompletionReview(
        Number(reviewFormData.maintenanceActivityId),
        payload
      );
      if (response?.decision === 'REWORK_REQUIRED') {
        setSuccess(
          'Completion Review recorded. A rework Issue has been created for managerial decision.'
        );
        setShowReworkDecisionLink(true);
      } else {
        setSuccess('Completion review recorded successfully.');
        setShowReworkDecisionLink(false);
      }
      setReviewFormData({
        maintenanceActivityId: '',
        decision: 'APPROVED',
        reviewNotes: '',
        reviewedAt: toDateTimeLocalValue(),
        reworkSeverity: 'MEDIUM',
        rootCause: '',
        correctiveAction: '',
        preventiveAction: '',
      });
      await loadPageData(workOrdersPage);
    } catch (err) {
      if (isForbidden(err)) {
        setError('You do not have permission to record completion reviews for this maintenance activity.');
      } else {
        setError(getApiErrorMessage(err, 'Failed to record completion review.'));
      }
    } finally {
      setReviewing(false);
    }
  };

  const handleLogout = () => {
    logout();
    navigate(ROUTES.LOGIN);
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
        <button type="button" className="back-btn" onClick={() => navigate(ROUTES.HOME)}>
          ← Back
        </button>
        <h1>Work Orders</h1>
        <div className="user-header-actions">
          <NotificationButton />
          {canExport && <ExportCsvButton exportType={REPORTING_EXPORT_TYPES.WORK_ORDERS} onError={setError} />}
          <button type="button" className="logout-btn" onClick={handleLogout}>
            Logout
          </button>
        </div>
      </header>

      <main className="reference-content work-orders-content">
        {error && <div className="error-message">{error}</div>}
        {success && (
          <div className="success-message">
            {success}
            {showReworkDecisionLink && (
              <>
                {' '}
                <Link to="/operational-decisions">Go to Operational Decisions</Link>
              </>
            )}
          </div>
        )}

        {canCreate ? (
          <CreateWorkOrderForm
            formData={formData}
            eligibleDecisions={decisions}
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
            createdWorkOrders={assignableWorkOrders}
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

              {isReworkRequired && (
                <>
                  <div className="form-row">
                    <label htmlFor="reworkSeverity">Rework Severity</label>
                    <select
                      id="reworkSeverity"
                      name="reworkSeverity"
                      value={reviewFormData.reworkSeverity}
                      onChange={handleReviewChange}
                      required
                      disabled={reviewing}
                    >
                      {ISSUE_SEVERITY_OPTIONS.map((option) => (
                        <option key={option.value} value={option.value}>
                          {option.label}
                        </option>
                      ))}
                    </select>
                  </div>

                  <div className="form-row">
                    <label htmlFor="rootCause">Root Cause</label>
                    <textarea
                      id="rootCause"
                      name="rootCause"
                      value={reviewFormData.rootCause}
                      onChange={handleReviewChange}
                      disabled={reviewing}
                      rows={2}
                    />
                  </div>

                  <div className="form-row">
                    <label htmlFor="correctiveAction">Corrective Action</label>
                    <textarea
                      id="correctiveAction"
                      name="correctiveAction"
                      value={reviewFormData.correctiveAction}
                      onChange={handleReviewChange}
                      disabled={reviewing}
                      rows={2}
                    />
                  </div>

                  <div className="form-row">
                    <label htmlFor="preventiveAction">Preventive Action</label>
                    <textarea
                      id="preventiveAction"
                      name="preventiveAction"
                      value={reviewFormData.preventiveAction}
                      onChange={handleReviewChange}
                      disabled={reviewing}
                      rows={2}
                    />
                  </div>
                </>
              )}

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
