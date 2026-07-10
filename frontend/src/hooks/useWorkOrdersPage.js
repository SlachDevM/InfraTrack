import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import apiClient from '../services/apiClient';
import workOrderApi from '../services/workOrderApi';
import maintenanceActivityApi from '../services/maintenanceActivityApi';
import operationalDecisionApi from '../services/operationalDecisionApi';
import { WORK_ORDER_STATUS } from '../constants/statuses';
import { ROUTES } from '../constants/routes';
import {
  canAssignWorkOrders,
  canCompleteMaintenance,
  canCreateWorkOrders,
  canRecordCompletionReview,
  USER_ROLES,
} from '../constants/userRoles';
import { getApiErrorMessage, isForbidden } from '../utils/apiError';
import {
  DEFAULT_PAGE,
  MAX_PAGE_SIZE,
  getPageNumber,
  getTotalPages,
  unwrapPageContent,
} from '../utils/pagination';
import {
  createInitialAssignFormData,
  createInitialCompleteFormData,
  createInitialFormData,
  createInitialReviewFormData,
} from '../pages/workOrders/constants';

export function useWorkOrdersPage() {
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
  const [formData, setFormData] = useState(createInitialFormData);
  const [assignFormData, setAssignFormData] = useState(createInitialAssignFormData);
  const [completeFormData, setCompleteFormData] = useState(createInitialCompleteFormData);
  const [reviewFormData, setReviewFormData] = useState(createInitialReviewFormData);

  const canCreate = canCreateWorkOrders(auth?.user?.role);
  const canAssign = canAssignWorkOrders(auth?.user?.role);
  const canComplete = canCompleteMaintenance(auth?.user?.role);
  const canReview = canRecordCompletionReview(auth?.user?.role);
  const isReworkRequired = reviewFormData.decision === 'REWORK_REQUIRED';
  const currentUserId = auth?.user?.userId;

  const selectedDecision = useMemo(
    () =>
      decisions.find((decision) => String(decision.id) === String(formData.operationalDecisionId)),
    [decisions, formData.operationalDecisionId]
  );

  const selectedAssignWorkOrder = useMemo(
    () =>
      assignableWorkOrders.find((order) => String(order.id) === String(assignFormData.workOrderId)),
    [assignableWorkOrders, assignFormData.workOrderId]
  );

  const assignedWorkOrdersForCurrentUser = useMemo(
    () =>
      workOrders.filter(
        (order) =>
          order.status === WORK_ORDER_STATUS.ASSIGNED &&
          currentUserId &&
          order.assignedToUserId === currentUserId
      ),
    [workOrders, currentUserId]
  );

  const selectedCompleteWorkOrder = useMemo(
    () => workOrders.find((order) => String(order.id) === String(completeFormData.workOrderId)),
    [workOrders, completeFormData.workOrderId]
  );

  const selectedReviewActivity = useMemo(
    () =>
      reviewableMaintenanceActivities.find(
        (activity) => String(activity.id) === String(reviewFormData.maintenanceActivityId)
      ),
    [reviewableMaintenanceActivities, reviewFormData.maintenanceActivityId]
  );

  useEffect(() => {
    if (!selectedAssignWorkOrder?.assetDepartmentId) {
      setEligibleAssignees([]);
      return;
    }
    const requiredRole =
      selectedAssignWorkOrder.workType === 'INTERNAL_MAINTENANCE'
        ? USER_ROLES.FIELD_EMPLOYEE
        : USER_ROLES.CONTRACTOR;
    workOrderApi
      .listEligibleWorkers(selectedAssignWorkOrder.assetDepartmentId, requiredRole)
      .then(setEligibleAssignees)
      .catch((err) => {
        setEligibleAssignees([]);
        setError(getApiErrorMessage(err, 'Failed to load eligible assignees.'));
      });
  }, [selectedAssignWorkOrder]);

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
      const [
        workOrderPage,
        decisionData,
        maintenanceActivityData,
        assignablePage,
        reviewableActivityData,
      ] = await Promise.all([
        workOrderApi.list(page),
        canCreate
          ? operationalDecisionApi.listEligibleForWorkOrderCreation(DEFAULT_PAGE, MAX_PAGE_SIZE)
          : Promise.resolve(null),
        canComplete
          ? maintenanceActivityApi.list(DEFAULT_PAGE, MAX_PAGE_SIZE)
          : Promise.resolve(null),
        canAssign
          ? workOrderApi.listEligibleForAssignment(DEFAULT_PAGE, MAX_PAGE_SIZE)
          : Promise.resolve(null),
        canReview
          ? maintenanceActivityApi.listEligibleForCompletionReview(DEFAULT_PAGE, MAX_PAGE_SIZE)
          : Promise.resolve(null),
      ]);
      setWorkOrders(unwrapPageContent(workOrderPage));
      setWorkOrdersPage(getPageNumber(workOrderPage, page));
      setWorkOrdersTotalPages(getTotalPages(workOrderPage));
      setDecisions(decisionData ? unwrapPageContent(decisionData) : []);
      setAssignableWorkOrders(assignablePage ? unwrapPageContent(assignablePage) : []);
      setMaintenanceActivities(
        maintenanceActivityData ? unwrapPageContent(maintenanceActivityData) : []
      );
      setReviewableMaintenanceActivities(
        reviewableActivityData ? unwrapPageContent(reviewableActivityData) : []
      );
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
      setFormData(createInitialFormData());
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
      setAssignFormData(createInitialAssignFormData());
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
      setCompleteFormData(createInitialCompleteFormData());
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
      setReviewFormData(createInitialReviewFormData());
      await loadPageData(workOrdersPage);
    } catch (err) {
      if (isForbidden(err)) {
        setError(
          'You do not have permission to record completion reviews for this maintenance activity.'
        );
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

  const goToPreviousPage = () => loadWorkOrders(workOrdersPage - 1);
  const goToNextPage = () => loadWorkOrders(workOrdersPage + 1);

  return {
    loading,
    error,
    success,
    setError,
    showReworkDecisionLink,
    canCreate,
    canAssign,
    canComplete,
    canReview,
    formData,
    decisions,
    selectedDecision,
    submitting,
    handleChange,
    handleSubmit,
    assignFormData,
    assignableWorkOrders,
    selectedAssignWorkOrder,
    eligibleAssignees,
    assigning,
    handleAssignChange,
    handleAssignSubmit,
    completeFormData,
    assignedWorkOrdersForCurrentUser,
    selectedCompleteWorkOrder,
    completing,
    handleCompleteChange,
    handleCompleteSubmit,
    reviewFormData,
    reviewableMaintenanceActivities,
    selectedReviewActivity,
    isReworkRequired,
    reviewing,
    handleReviewChange,
    handleReviewSubmit,
    workOrders,
    maintenanceActivities,
    workOrdersPage,
    workOrdersTotalPages,
    listLoading,
    goToPreviousPage,
    goToNextPage,
    handleLogout,
    navigate,
  };
}
