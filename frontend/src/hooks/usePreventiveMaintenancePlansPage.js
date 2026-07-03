import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import apiClient from '../services/apiClient';
import preventiveMaintenancePlanApi from '../services/preventiveMaintenancePlanApi';
import assetApi from '../services/assetApi';
import inspectionTemplateApi from '../services/inspectionTemplateApi';
import {
  canManagePreventiveMaintenancePlans,
  canViewPreventiveMaintenancePlans,
} from '../constants/userRoles';
import { ROUTES } from '../constants/routes';
import {
  buildTriggerSummaryPreview,
  isValidPlanCode,
  parseTriggerConfiguration,
} from '../utils/planTriggerConfiguration';
import { getApiErrorMessage, isForbidden } from '../utils/apiError';
import {
  DEFAULT_PAGE,
  MAX_PAGE_SIZE,
  getPageNumber,
  getTotalPages,
  unwrapPageContent,
} from '../utils/pagination';
import { DEFAULT_FORM, buildPayload } from '../pages/preventiveMaintenancePlans/constants';

export function usePreventiveMaintenancePlansPage() {
  const navigate = useNavigate();
  const { auth } = useAuth();
  const [plans, setPlans] = useState([]);
  const [assets, setAssets] = useState([]);
  const [templates, setTemplates] = useState([]);
  const [plansPage, setPlansPage] = useState(DEFAULT_PAGE);
  const [plansTotalPages, setPlansTotalPages] = useState(0);
  const [listLoading, setListLoading] = useState(false);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [filterAssetId, setFilterAssetId] = useState('');
  const [filterStatus, setFilterStatus] = useState('');
  const [filterTriggerType, setFilterTriggerType] = useState('');
  const [formData, setFormData] = useState(DEFAULT_FORM);
  const [editingId, setEditingId] = useState(null);
  const [evaluationResults, setEvaluationResults] = useState({});
  const [evaluatingId, setEvaluatingId] = useState(null);

  const canView = canViewPreventiveMaintenancePlans(auth?.user?.role);
  const canManage = canManagePreventiveMaintenancePlans(auth?.user?.role);
  const triggerSummaryPreview = buildTriggerSummaryPreview(formData.triggerType, formData);

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
    assetId: filterAssetId ? Number(filterAssetId) : undefined,
    status: filterStatus || undefined,
    triggerType: filterTriggerType || undefined,
  });

  const loadPlansWithFilters = async (page, assetFilter, statusFilter, triggerTypeFilter) => {
    try {
      setListLoading(true);
      setError(null);
      const filters = {
        assetId: assetFilter ? Number(assetFilter) : undefined,
        status: statusFilter || undefined,
        triggerType: triggerTypeFilter || undefined,
      };
      const planPage = await preventiveMaintenancePlanApi.list(page, undefined, filters);
      setPlans(unwrapPageContent(planPage));
      setPlansPage(getPageNumber(planPage, page));
      setPlansTotalPages(getTotalPages(planPage));
    } catch (err) {
      setError(getApiErrorMessage(err, 'Failed to load preventive maintenance plans.'));
    } finally {
      setListLoading(false);
    }
  };

  const loadPlans = async (page = plansPage) => {
    await loadPlansWithFilters(page, filterAssetId, filterStatus, filterTriggerType);
  };

  const loadPageData = async (page = plansPage) => {
    try {
      setLoading(true);
      setError(null);
      const [planPage, assetPage, templatePage] = await Promise.all([
        preventiveMaintenancePlanApi.list(page, undefined, buildFilters()),
        assetApi.list(DEFAULT_PAGE, MAX_PAGE_SIZE),
        inspectionTemplateApi.list(DEFAULT_PAGE, MAX_PAGE_SIZE),
      ]);
      setPlans(unwrapPageContent(planPage));
      setPlansPage(getPageNumber(planPage, page));
      setPlansTotalPages(getTotalPages(planPage));
      setAssets(unwrapPageContent(assetPage));
      setTemplates(unwrapPageContent(templatePage));
    } catch (err) {
      setError(getApiErrorMessage(err, 'Failed to load preventive maintenance plans.'));
    } finally {
      setLoading(false);
      setListLoading(false);
    }
  };

  const handleFilterChange = (e) => {
    const { name, value } = e.target;
    const nextAsset = name === 'filterAssetId' ? value : filterAssetId;
    const nextStatus = name === 'filterStatus' ? value : filterStatus;
    const nextTriggerType = name === 'filterTriggerType' ? value : filterTriggerType;
    if (name === 'filterAssetId') setFilterAssetId(value);
    else if (name === 'filterStatus') setFilterStatus(value);
    else if (name === 'filterTriggerType') setFilterTriggerType(value);
    loadPlansWithFilters(DEFAULT_PAGE, nextAsset, nextStatus, nextTriggerType);
  };

  const handleFormChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value,
    }));
  };

  const resetForm = () => {
    setFormData(DEFAULT_FORM);
    setEditingId(null);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!canManage) return;

    if (!editingId && !isValidPlanCode(formData.planCode)) {
      setError('Plan code must be uppercase snake_case (for example PUMP_MONTHLY).');
      return;
    }

    try {
      setSubmitting(true);
      setError(null);
      setSuccess(null);
      const payload = buildPayload(formData, Boolean(editingId));
      if (editingId) {
        await preventiveMaintenancePlanApi.update(editingId, payload);
        setSuccess('Preventive maintenance plan updated successfully.');
      } else {
        await preventiveMaintenancePlanApi.create(payload);
        setSuccess('Preventive maintenance plan created successfully.');
      }
      resetForm();
      await loadPlans(plansPage);
    } catch (err) {
      if (isForbidden(err)) {
        setError('You do not have permission to manage preventive maintenance plans.');
      } else {
        setError(getApiErrorMessage(err, 'Failed to save preventive maintenance plan.'));
      }
    } finally {
      setSubmitting(false);
    }
  };

  const handleEdit = async (plan) => {
    try {
      setError(null);
      const detail = await preventiveMaintenancePlanApi.get(plan.id);
      const triggerType = detail.businessTrigger?.triggerType || 'TIME';
      const triggerFields = parseTriggerConfiguration(
        triggerType,
        detail.businessTrigger?.configurationJson
      );
      setEditingId(plan.id);
      setFormData({
        planCode: detail.planCode,
        version: String(detail.version ?? 1),
        name: detail.name,
        description: detail.description || '',
        assetId: String(detail.assetId),
        status: detail.status,
        priority: detail.priority,
        targetAction: detail.targetAction,
        inspectionTemplateId: detail.inspectionTemplateId
          ? String(detail.inspectionTemplateId)
          : '',
        triggerType,
        ...triggerFields,
        triggerActive: detail.businessTrigger?.active ?? true,
      });
    } catch (err) {
      setError(getApiErrorMessage(err, 'Failed to load plan details for editing.'));
    }
  };

  const handleEvaluate = async (planId) => {
    try {
      setEvaluatingId(planId);
      setError(null);
      const result = await preventiveMaintenancePlanApi.evaluate(planId);
      setEvaluationResults((prev) => ({ ...prev, [planId]: result }));
    } catch (err) {
      setError(getApiErrorMessage(err, 'Failed to evaluate preventive maintenance plan.'));
    } finally {
      setEvaluatingId(null);
    }
  };

  const handleArchive = async (id) => {
    if (!window.confirm('Archive this preventive maintenance plan?')) return;

    try {
      setError(null);
      setSuccess(null);
      await preventiveMaintenancePlanApi.archive(id);
      setSuccess('Preventive maintenance plan archived successfully.');
      if (editingId === id) {
        resetForm();
      }
      await loadPlans(plansPage);
    } catch (err) {
      if (isForbidden(err)) {
        setError('You do not have permission to archive preventive maintenance plans.');
      } else {
        setError(getApiErrorMessage(err, 'Failed to archive preventive maintenance plan.'));
      }
    }
  };

  const goToPreviousPage = () => loadPlans(plansPage - 1);
  const goToNextPage = () => loadPlans(plansPage + 1);

  return {
    loading,
    error,
    success,
    canManage,
    filterAssetId,
    filterStatus,
    filterTriggerType,
    assets,
    listLoading,
    handleFilterChange,
    editingId,
    formData,
    submitting,
    templates,
    triggerSummaryPreview,
    handleFormChange,
    handleSubmit,
    resetForm,
    plans,
    evaluationResults,
    evaluatingId,
    handleEvaluate,
    handleEdit,
    handleArchive,
    plansPage,
    plansTotalPages,
    goToPreviousPage,
    goToNextPage,
  };
}
