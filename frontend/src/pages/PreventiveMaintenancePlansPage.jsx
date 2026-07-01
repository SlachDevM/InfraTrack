import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import apiClient from '../services/apiClient';
import preventiveMaintenancePlanApi from '../services/preventiveMaintenancePlanApi';
import assetApi from '../services/assetApi';
import inspectionTemplateApi from '../services/inspectionTemplateApi';
import ReferenceDataLayout from '../components/layout/ReferenceDataLayout';
import PaginationControls from '../components/PaginationControls';
import {
  canManagePreventiveMaintenancePlans,
  canViewPreventiveMaintenancePlans,
} from '../constants/userRoles';
import { ROUTES } from '../constants/routes';
import {
  PREVENTIVE_MAINTENANCE_PLAN_STATUS_OPTIONS,
  getPreventiveMaintenancePlanStatusLabel,
} from '../constants/preventiveMaintenancePlanStatuses';
import {
  PREVENTIVE_MAINTENANCE_PLAN_PRIORITY_OPTIONS,
  getPreventiveMaintenancePlanPriorityLabel,
} from '../constants/preventiveMaintenancePlanPriorities';
import {
  PLAN_TRIGGER_TYPE_OPTIONS,
} from '../constants/planTriggerTypes';
import {
  PLAN_TARGET_ACTION_OPTIONS,
  getPlanTargetActionLabel,
} from '../constants/planTargetActions';
import { PLAN_TIME_UNIT_OPTIONS } from '../constants/planTimeUnits';
import { PLAN_METER_TYPE_OPTIONS } from '../constants/planMeterTypes';
import { PLAN_EVENT_TYPE_OPTIONS } from '../constants/planEventTypes';
import {
  buildTriggerConfiguration,
  buildTriggerSummaryPreview,
  isValidPlanCode,
  normalizePlanCode,
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

const DEFAULT_FORM = {
  planCode: '',
  version: '1',
  name: '',
  description: '',
  assetId: '',
  status: 'DRAFT',
  priority: 'MEDIUM',
  targetAction: 'CREATE_INSPECTION',
  inspectionTemplateId: '',
  triggerType: 'TIME',
  timeEvery: '1',
  timeUnit: 'MONTH',
  meterType: 'OPERATING_HOURS',
  meterEvery: '250',
  eventType: 'COMPLETION_REVIEW',
  triggerActive: true,
};

function formatTimestamp(timestamp) {
  if (!timestamp) {
    return '-';
  }
  return new Date(timestamp).toLocaleString();
}

function buildPayload(formData, editing) {
  const payload = {
    name: formData.name.trim(),
    description: formData.description.trim() || undefined,
    assetId: Number(formData.assetId),
    status: formData.status,
    priority: formData.priority,
    targetAction: formData.targetAction,
    inspectionTemplateId: formData.inspectionTemplateId
      ? Number(formData.inspectionTemplateId)
      : undefined,
    businessTrigger: {
      triggerType: formData.triggerType,
      configurationJson: buildTriggerConfiguration(formData.triggerType, formData),
      active: formData.triggerActive,
    },
  };
  if (editing) {
    payload.version = Number(formData.version);
  } else {
    payload.planCode = normalizePlanCode(formData.planCode);
    if (formData.version) {
      payload.version = Number(formData.version);
    }
  }
  return payload;
}

export default function PreventiveMaintenancePlansPage() {
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

  const triggerSummaryPreview = buildTriggerSummaryPreview(formData.triggerType, formData);

  if (loading) {
    return <div className="loading">Loading preventive maintenance plans...</div>;
  }

  return (
    <ReferenceDataLayout title="Preventive Maintenance Plans">
        {error && <div className="error-message">{error}</div>}
        {success && <div className="success-message">{success}</div>}

        {!canManage && (
          <p className="read-only-note">
            Preventive maintenance plans are read-only. Administrators can create, edit, and archive plans.
          </p>
        )}

        <section className="reference-form-section">
          <h2>Filters</h2>
          <div className="filter-row">
            <div className="form-row">
              <label htmlFor="filterAssetId">Asset</label>
              <select
                id="filterAssetId"
                name="filterAssetId"
                value={filterAssetId}
                onChange={handleFilterChange}
                disabled={listLoading}
              >
                <option value="">All assets</option>
                {assets.map((asset) => (
                  <option key={asset.id} value={asset.id}>
                    {asset.name}
                  </option>
                ))}
              </select>
            </div>
            <div className="form-row">
              <label htmlFor="filterStatus">Status</label>
              <select
                id="filterStatus"
                name="filterStatus"
                value={filterStatus}
                onChange={handleFilterChange}
                disabled={listLoading}
              >
                <option value="">All statuses</option>
                {PREVENTIVE_MAINTENANCE_PLAN_STATUS_OPTIONS.map((option) => (
                  <option key={option.value} value={option.value}>
                    {option.label}
                  </option>
                ))}
              </select>
            </div>
            <div className="form-row">
              <label htmlFor="filterTriggerType">Trigger Type</label>
              <select
                id="filterTriggerType"
                name="filterTriggerType"
                value={filterTriggerType}
                onChange={handleFilterChange}
                disabled={listLoading}
              >
                <option value="">All trigger types</option>
                {PLAN_TRIGGER_TYPE_OPTIONS.map((option) => (
                  <option key={option.value} value={option.value}>
                    {option.label}
                  </option>
                ))}
              </select>
            </div>
          </div>
        </section>

        {canManage && (
          <section className="reference-form-section">
            <h2>{editingId ? 'Edit Preventive Maintenance Plan' : 'Create Preventive Maintenance Plan'}</h2>
            <form className="reference-form reference-form-stacked" onSubmit={handleSubmit}>
              <div className="reference-form-grid">
                <div className="form-row">
                  <label htmlFor="planCode">Plan Code</label>
                  <input
                    id="planCode"
                    name="planCode"
                    type="text"
                    value={formData.planCode}
                    onChange={handleFormChange}
                    required={!editingId}
                    disabled={submitting || Boolean(editingId)}
                    placeholder="PUMP_MONTHLY"
                  />
                </div>
                <div className="form-row">
                  <label htmlFor="version">Version</label>
                  <input
                    id="version"
                    name="version"
                    type="number"
                    min="1"
                    value={formData.version}
                    onChange={handleFormChange}
                    required
                    disabled={submitting}
                  />
                </div>
                <div className="form-row">
                  <label htmlFor="name">Name</label>
                  <input
                    id="name"
                    name="name"
                    type="text"
                    value={formData.name}
                    onChange={handleFormChange}
                    required
                    disabled={submitting}
                  />
                </div>
              </div>

              <div className="form-row form-row-full">
                <label htmlFor="description">Description</label>
                <textarea
                  id="description"
                  name="description"
                  value={formData.description}
                  onChange={handleFormChange}
                  disabled={submitting}
                  rows={3}
                />
              </div>

              <div className="reference-form-grid">
                <div className="form-row">
                  <label htmlFor="assetId">Asset</label>
                  <select
                    id="assetId"
                    name="assetId"
                    value={formData.assetId}
                    onChange={handleFormChange}
                    required={!editingId}
                    disabled={submitting || Boolean(editingId)}
                  >
                    <option value="">Select asset</option>
                    {assets.map((asset) => (
                      <option key={asset.id} value={asset.id}>
                        {asset.name}
                      </option>
                    ))}
                  </select>
                </div>
                <div className="form-row">
                  <label htmlFor="status">Status</label>
                  <select
                    id="status"
                    name="status"
                    value={formData.status}
                    onChange={handleFormChange}
                    required
                    disabled={submitting}
                  >
                    {PREVENTIVE_MAINTENANCE_PLAN_STATUS_OPTIONS.map((option) => (
                      <option key={option.value} value={option.value}>
                        {option.label}
                      </option>
                    ))}
                  </select>
                </div>
                <div className="form-row">
                  <label htmlFor="priority">Priority</label>
                  <select
                    id="priority"
                    name="priority"
                    value={formData.priority}
                    onChange={handleFormChange}
                    required
                    disabled={submitting}
                  >
                    {PREVENTIVE_MAINTENANCE_PLAN_PRIORITY_OPTIONS.map((option) => (
                      <option key={option.value} value={option.value}>
                        {option.label}
                      </option>
                    ))}
                  </select>
                </div>
                <div className="form-row">
                  <label htmlFor="targetAction">Target Action</label>
                  <select
                    id="targetAction"
                    name="targetAction"
                    value={formData.targetAction}
                    onChange={handleFormChange}
                    required
                    disabled={submitting}
                  >
                    {PLAN_TARGET_ACTION_OPTIONS.map((option) => (
                      <option key={option.value} value={option.value}>
                        {option.label}
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              <div className="reference-form-grid">
                <div className="form-row">
                  <label htmlFor="triggerType">Trigger Type</label>
                  <select
                    id="triggerType"
                    name="triggerType"
                    value={formData.triggerType}
                    onChange={handleFormChange}
                    required
                    disabled={submitting}
                  >
                    {PLAN_TRIGGER_TYPE_OPTIONS.map((option) => (
                      <option key={option.value} value={option.value}>
                        {option.label}
                      </option>
                    ))}
                  </select>
                </div>
              {formData.triggerType === 'TIME' && (
                <>
                  <div className="form-row">
                    <label htmlFor="timeEvery">Every</label>
                    <input
                      id="timeEvery"
                      name="timeEvery"
                      type="number"
                      min="1"
                      value={formData.timeEvery}
                      onChange={handleFormChange}
                      required
                      disabled={submitting}
                    />
                  </div>
                  <div className="form-row">
                    <label htmlFor="timeUnit">Unit</label>
                    <select
                      id="timeUnit"
                      name="timeUnit"
                      value={formData.timeUnit}
                      onChange={handleFormChange}
                      required
                      disabled={submitting}
                    >
                      {PLAN_TIME_UNIT_OPTIONS.map((option) => (
                        <option key={option.value} value={option.value}>
                          {option.label}
                        </option>
                      ))}
                    </select>
                  </div>
                </>
              )}
              {formData.triggerType === 'METER' && (
                <>
                  <div className="form-row">
                    <label htmlFor="meterType">Meter</label>
                    <select
                      id="meterType"
                      name="meterType"
                      value={formData.meterType}
                      onChange={handleFormChange}
                      required
                      disabled={submitting}
                    >
                      {PLAN_METER_TYPE_OPTIONS.map((option) => (
                        <option key={option.value} value={option.value}>
                          {option.label}
                        </option>
                      ))}
                    </select>
                  </div>
                  <div className="form-row">
                    <label htmlFor="meterEvery">Every</label>
                    <input
                      id="meterEvery"
                      name="meterEvery"
                      type="number"
                      min="1"
                      value={formData.meterEvery}
                      onChange={handleFormChange}
                      required
                      disabled={submitting}
                    />
                  </div>
                </>
              )}
              {formData.triggerType === 'EVENT' && (
                <div className="form-row">
                  <label htmlFor="eventType">Event</label>
                  <select
                    id="eventType"
                    name="eventType"
                    value={formData.eventType}
                    onChange={handleFormChange}
                    required
                    disabled={submitting}
                  >
                    {PLAN_EVENT_TYPE_OPTIONS.map((option) => (
                      <option key={option.value} value={option.value}>
                        {option.label}
                      </option>
                    ))}
                  </select>
                </div>
              )}
              </div>

              {formData.targetAction === 'CREATE_INSPECTION' && (
                <div className="form-row form-row-full">
                  <label htmlFor="inspectionTemplateId">Inspection Template (optional)</label>
                  <select
                    id="inspectionTemplateId"
                    name="inspectionTemplateId"
                    value={formData.inspectionTemplateId}
                    onChange={handleFormChange}
                    disabled={submitting}
                  >
                    <option value="">None</option>
                    {templates.map((template) => (
                      <option key={template.id} value={template.id}>
                        {template.name}
                      </option>
                    ))}
                  </select>
                </div>
              )}

              {triggerSummaryPreview && (
                <p className="read-only-note">
                  Trigger summary:
                  {' '}
                  <strong>{triggerSummaryPreview}</strong>
                </p>
              )}
              <div className="form-row">
                <label htmlFor="triggerActive">
                  <input
                    id="triggerActive"
                    name="triggerActive"
                    type="checkbox"
                    checked={formData.triggerActive}
                    onChange={handleFormChange}
                    disabled={submitting}
                  />
                  {' '}
                  Trigger active
                </label>
              </div>
              <div className="form-actions">
                <button type="submit" className="btn-primary" disabled={submitting}>
                  {submitting ? 'Saving...' : editingId ? 'Update Plan' : 'Create Plan'}
                </button>
                {editingId && (
                  <button
                    type="button"
                    className="btn-secondary"
                    onClick={resetForm}
                    disabled={submitting}
                  >
                    Cancel Edit
                  </button>
                )}
              </div>
            </form>
          </section>
        )}

        <section>
          <h2>Plans</h2>
          {plans.length === 0 ? (
            <p className="no-items">No preventive maintenance plans found.</p>
          ) : (
            <table className="reference-table">
              <thead>
                <tr>
                  <th>Plan Code</th>
                  <th>Name</th>
                  <th>Asset</th>
                  <th>Trigger Summary</th>
                  <th>Target Action</th>
                  <th>Status</th>
                  <th>Priority</th>
                  <th>Updated</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {plans.map((plan) => (
                  <tr key={plan.id}>
                    <td>{plan.planCode}</td>
                    <td>{plan.name}</td>
                    <td>{plan.assetName}</td>
                    <td title={plan.businessTrigger?.triggerSummary?.description}>
                      {plan.businessTrigger?.triggerSummary?.title || '-'}
                    </td>
                    <td>{getPlanTargetActionLabel(plan.targetAction)}</td>
                    <td>{getPreventiveMaintenancePlanStatusLabel(plan.status)}</td>
                    <td>{getPreventiveMaintenancePlanPriorityLabel(plan.priority)}</td>
                    <td>{formatTimestamp(plan.updatedAt)}</td>
                    <td>
                      <button
                        type="button"
                        className="btn-link"
                        onClick={() => handleEvaluate(plan.id)}
                        disabled={evaluatingId === plan.id}
                      >
                        {evaluatingId === plan.id ? 'Evaluating...' : 'Evaluate'}
                      </button>
                      {evaluationResults[plan.id] && (
                        <div className="evaluation-result">
                          <span
                            className={
                              evaluationResults[plan.id].eligible
                                ? 'evaluation-badge evaluation-badge-eligible'
                                : 'evaluation-badge evaluation-badge-not-eligible'
                            }
                          >
                            {evaluationResults[plan.id].eligible ? 'Eligible' : 'Not eligible'}
                          </span>
                          <div className="evaluation-reason">
                            {evaluationResults[plan.id].evaluationReason}
                          </div>
                          <div className="evaluation-timestamp">
                            Evaluated:
                            {' '}
                            {formatTimestamp(evaluationResults[plan.id].evaluatedAt)}
                          </div>
                          {evaluationResults[plan.id].nextEligibleAt && (
                            <div className="evaluation-timestamp">
                              Next eligible:
                              {' '}
                              {formatTimestamp(evaluationResults[plan.id].nextEligibleAt)}
                            </div>
                          )}
                        </div>
                      )}
                      {canManage && plan.status !== 'ARCHIVED' && (
                        <>
                          {' '}
                          <button
                            type="button"
                            className="btn-link"
                            onClick={() => handleEdit(plan)}
                          >
                            Edit
                          </button>
                          {' '}
                          <button
                            type="button"
                            className="btn-link"
                            onClick={() => handleArchive(plan.id)}
                          >
                            Archive
                          </button>
                        </>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </section>

        <PaginationControls
          page={plansPage}
          totalPages={plansTotalPages}
          loading={listLoading}
          onPrevious={() => loadPlans(plansPage - 1)}
          onNext={() => loadPlans(plansPage + 1)}
        />
    </ReferenceDataLayout>
  );
}
