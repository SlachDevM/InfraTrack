import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import apiClient from '../services/apiClient';
import preventiveExecutionCandidateApi from '../services/preventiveExecutionCandidateApi';
import preventiveMaintenancePlanApi from '../services/preventiveMaintenancePlanApi';
import assetApi from '../services/assetApi';
import inspectionApi from '../services/inspectionApi';
import userApi from '../services/userApi';
import {
  canGeneratePreventiveExecutionCandidates,
  canReviewPreventiveExecutionCandidates,
  canViewPreventiveExecutionCandidates,
} from '../constants/userRoles';
import { ROUTES } from '../constants/routes';
import { getApiErrorMessage, isForbidden } from '../utils/apiError';
import { filterInspectionAssignees } from '../utils/inspectionAssignees';
import {
  DEFAULT_PAGE,
  MAX_PAGE_SIZE,
  getPageNumber,
  getTotalPages,
  unwrapPageContent,
} from '../utils/pagination';
import {
  EMPTY_APPROVE_FORM,
  dateInputToPlannedAt,
  parseAssigneeId,
} from '../pages/preventiveExecutionCandidates/constants';

export function usePreventiveExecutionCandidatesPage() {
  const navigate = useNavigate();
  const { auth } = useAuth();
  const [candidates, setCandidates] = useState([]);
  const [assets, setAssets] = useState([]);
  const [plans, setPlans] = useState([]);
  const [candidatesPage, setCandidatesPage] = useState(DEFAULT_PAGE);
  const [candidatesTotalPages, setCandidatesTotalPages] = useState(0);
  const [listLoading, setListLoading] = useState(false);
  const [loading, setLoading] = useState(true);
  const [generating, setGenerating] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [filterStatus, setFilterStatus] = useState('');
  const [filterAssetId, setFilterAssetId] = useState('');
  const [filterPlanId, setFilterPlanId] = useState('');
  const [selectedCandidate, setSelectedCandidate] = useState(null);
  const [selectedReport, setSelectedReport] = useState(null);
  const [detailTab, setDetailTab] = useState('candidate');
  const [detailLoading, setDetailLoading] = useState(false);
  const [workers, setWorkers] = useState([]);
  const [reviewing, setReviewing] = useState(false);
  const [approveCandidate, setApproveCandidate] = useState(null);
  const [rejectCandidate, setRejectCandidate] = useState(null);
  const [dismissCandidate, setDismissCandidate] = useState(null);
  const [approveForm, setApproveForm] = useState(EMPTY_APPROVE_FORM);
  const [rejectReason, setRejectReason] = useState('');
  const [dismissComment, setDismissComment] = useState('');
  const [createdInspectionId, setCreatedInspectionId] = useState(null);

  const canView = canViewPreventiveExecutionCandidates(auth?.user?.role);
  const canGenerate = canGeneratePreventiveExecutionCandidates(auth?.user?.role);
  const canReview = canReviewPreventiveExecutionCandidates(auth?.user?.role);

  const selectedAsset = useMemo(
    () => assets.find((asset) => asset.id === approveCandidate?.assetId),
    [assets, approveCandidate]
  );

  const selectedAssigneeId = parseAssigneeId(approveForm.assigneeId);

  useEffect(() => {
    if (!auth) {
      navigate(ROUTES.LOGIN);
      return;
    }
    if (!canView) {
      navigate(ROUTES.HOME);
      return;
    }
    apiClient.setToken(auth.token);
    loadPageData();
  }, [auth, canView, navigate]);

  const buildFilters = () => ({
    status: filterStatus || undefined,
    assetId: filterAssetId ? Number(filterAssetId) : undefined,
    planId: filterPlanId ? Number(filterPlanId) : undefined,
  });

  const loadCandidatesWithFilters = async (page, statusFilter, assetFilter, planFilter) => {
    try {
      setListLoading(true);
      setError(null);
      const filters = {
        status: statusFilter || undefined,
        assetId: assetFilter ? Number(assetFilter) : undefined,
        planId: planFilter ? Number(planFilter) : undefined,
      };
      const candidatePage = await preventiveExecutionCandidateApi.list(page, undefined, filters);
      setCandidates(unwrapPageContent(candidatePage));
      setCandidatesPage(getPageNumber(candidatePage, page));
      setCandidatesTotalPages(getTotalPages(candidatePage));
    } catch (err) {
      setError(getApiErrorMessage(err, 'Failed to load preventive execution candidates.'));
    } finally {
      setListLoading(false);
    }
  };

  const loadPageData = async (page = candidatesPage) => {
    try {
      setLoading(true);
      setError(null);
      const [candidatePage, assetPage, planPage] = await Promise.all([
        preventiveExecutionCandidateApi.list(page, undefined, buildFilters()),
        assetApi.list(DEFAULT_PAGE, MAX_PAGE_SIZE),
        preventiveMaintenancePlanApi.list(DEFAULT_PAGE, MAX_PAGE_SIZE),
      ]);
      setCandidates(unwrapPageContent(candidatePage));
      setCandidatesPage(getPageNumber(candidatePage, page));
      setCandidatesTotalPages(getTotalPages(candidatePage));
      setAssets(unwrapPageContent(assetPage));
      setPlans(unwrapPageContent(planPage));
      if (canReview) {
        const profile = await userApi.getCurrentUser();
        const workerData = await inspectionApi.listWorkers();
        const departmentId = selectedAsset?.departmentId ?? profile?.departmentId;
        setWorkers(filterInspectionAssignees(workerData, departmentId));
      }
    } catch (err) {
      setError(getApiErrorMessage(err, 'Failed to load preventive execution candidates.'));
    } finally {
      setLoading(false);
      setListLoading(false);
    }
  };

  const handleFilterChange = (e) => {
    const { name, value } = e.target;
    const nextStatus = name === 'filterStatus' ? value : filterStatus;
    const nextAsset = name === 'filterAssetId' ? value : filterAssetId;
    const nextPlan = name === 'filterPlanId' ? value : filterPlanId;
    if (name === 'filterStatus') setFilterStatus(value);
    else if (name === 'filterAssetId') setFilterAssetId(value);
    else if (name === 'filterPlanId') setFilterPlanId(value);
    loadCandidatesWithFilters(DEFAULT_PAGE, nextStatus, nextAsset, nextPlan);
  };

  const handleGenerate = async () => {
    if (!canGenerate) return;

    try {
      setGenerating(true);
      setError(null);
      setSuccess(null);
      const results = await preventiveExecutionCandidateApi.generate();
      const created = results.filter((result) => result.outcome === 'CREATED').length;
      const skipped = results.filter((result) => result.outcome === 'SKIPPED_DUPLICATE').length;
      setSuccess(
        `Generation complete: ${created} created, ${skipped} skipped (existing pending), ` +
          `${results.length - created - skipped} not eligible.`
      );
      await loadCandidatesWithFilters(DEFAULT_PAGE, filterStatus, filterAssetId, filterPlanId);
    } catch (err) {
      if (isForbidden(err)) {
        setError('You do not have permission to generate preventive execution candidates.');
      } else {
        setError(getApiErrorMessage(err, 'Failed to generate preventive execution candidates.'));
      }
    } finally {
      setGenerating(false);
    }
  };

  const handleViewDetail = async (candidateId) => {
    try {
      setDetailLoading(true);
      setError(null);
      setDetailTab('candidate');
      const [detail, report] = await Promise.all([
        preventiveExecutionCandidateApi.get(candidateId),
        preventiveExecutionCandidateApi.getReport(candidateId),
      ]);
      setSelectedCandidate(detail);
      setSelectedReport(report);
    } catch (err) {
      setError(getApiErrorMessage(err, 'Failed to load candidate details.'));
    } finally {
      setDetailLoading(false);
    }
  };

  const reloadSelectedDetail = async (candidateId) => {
    const [detail, report] = await Promise.all([
      preventiveExecutionCandidateApi.get(candidateId),
      preventiveExecutionCandidateApi.getReport(candidateId),
    ]);
    setSelectedCandidate(detail);
    setSelectedReport(report);
  };

  const openApproveDialog = async (candidate) => {
    setApproveCandidate(candidate);
    setApproveForm(EMPTY_APPROVE_FORM);
    setCreatedInspectionId(null);
    if (canReview) {
      try {
        const profile = await userApi.getCurrentUser();
        const asset = assets.find((item) => item.id === candidate.assetId);
        const workerData = await inspectionApi.listWorkers();
        setWorkers(
          filterInspectionAssignees(workerData, asset?.departmentId ?? profile?.departmentId)
        );
      } catch {
        setWorkers([]);
      }
    }
  };

  const handleApproveSubmit = async (e) => {
    e.preventDefault();
    if (!approveCandidate || !canReview || selectedAssigneeId == null) return;

    try {
      setReviewing(true);
      setError(null);
      setSuccess(null);
      const response = await preventiveExecutionCandidateApi.approve(approveCandidate.id, {
        assigneeId: selectedAssigneeId,
        plannedAt: dateInputToPlannedAt(approveForm.plannedAt),
        notes: approveForm.notes.trim() || undefined,
      });
      setCreatedInspectionId(response.inspection?.id ?? response.candidate?.createdInspectionId);
      setSuccess('Candidate approved and inspection created.');
      setApproveCandidate(null);
      setSelectedCandidate(response.candidate);
      await reloadSelectedDetail(response.candidate.id);
      await loadCandidatesWithFilters(candidatesPage, filterStatus, filterAssetId, filterPlanId);
    } catch (err) {
      setError(getApiErrorMessage(err, 'Failed to approve preventive execution candidate.'));
    } finally {
      setReviewing(false);
    }
  };

  const handleRejectSubmit = async (e) => {
    e.preventDefault();
    if (!rejectCandidate || !canReview) return;

    try {
      setReviewing(true);
      setError(null);
      setSuccess(null);
      const response = await preventiveExecutionCandidateApi.reject(rejectCandidate.id, {
        reason: rejectReason.trim() || undefined,
      });
      setSuccess('Candidate rejected.');
      setRejectCandidate(null);
      setRejectReason('');
      setSelectedCandidate(response);
      await reloadSelectedDetail(response.id);
      await loadCandidatesWithFilters(candidatesPage, filterStatus, filterAssetId, filterPlanId);
    } catch (err) {
      setError(getApiErrorMessage(err, 'Failed to reject preventive execution candidate.'));
    } finally {
      setReviewing(false);
    }
  };

  const handleDismissSubmit = async (e) => {
    e.preventDefault();
    if (!dismissCandidate || !canReview) return;

    try {
      setReviewing(true);
      setError(null);
      setSuccess(null);
      const response = await preventiveExecutionCandidateApi.dismiss(dismissCandidate.id, {
        comment: dismissComment.trim() || undefined,
      });
      setSuccess('Candidate dismissed.');
      setDismissCandidate(null);
      setDismissComment('');
      setSelectedCandidate(response);
      await reloadSelectedDetail(response.id);
      await loadCandidatesWithFilters(candidatesPage, filterStatus, filterAssetId, filterPlanId);
    } catch (err) {
      setError(getApiErrorMessage(err, 'Failed to dismiss preventive execution candidate.'));
    } finally {
      setReviewing(false);
    }
  };

  const openRejectDialog = (candidate) => {
    setRejectCandidate(candidate);
    setRejectReason('');
  };

  const openDismissDialog = (candidate) => {
    setDismissCandidate(candidate);
    setDismissComment('');
  };

  const closeDetail = () => {
    setSelectedCandidate(null);
    setSelectedReport(null);
  };

  const goToPreviousPage = () =>
    loadCandidatesWithFilters(candidatesPage - 1, filterStatus, filterAssetId, filterPlanId);

  const goToNextPage = () =>
    loadCandidatesWithFilters(candidatesPage + 1, filterStatus, filterAssetId, filterPlanId);

  const handleApproveFormChange = (field, value) => {
    setApproveForm((prev) => ({
      ...prev,
      [field]: value,
    }));
  };

  return {
    loading,
    error,
    success,
    setError,
    canGenerate,
    canReview,
    generating,
    handleGenerate,
    filterStatus,
    filterAssetId,
    filterPlanId,
    assets,
    plans,
    handleFilterChange,
    listLoading,
    candidates,
    candidatesPage,
    candidatesTotalPages,
    goToPreviousPage,
    goToNextPage,
    selectedCandidate,
    selectedReport,
    detailTab,
    setDetailTab,
    detailLoading,
    closeDetail,
    handleViewDetail,
    openApproveDialog,
    openRejectDialog,
    openDismissDialog,
    approveCandidate,
    rejectCandidate,
    dismissCandidate,
    approveForm,
    rejectReason,
    dismissComment,
    createdInspectionId,
    workers,
    reviewing,
    selectedAssigneeId,
    handleApproveSubmit,
    handleRejectSubmit,
    handleDismissSubmit,
    setApproveCandidate,
    setRejectCandidate,
    setDismissCandidate,
    setRejectReason,
    setDismissComment,
    handleApproveFormChange,
  };
}
