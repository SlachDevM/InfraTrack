import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import apiClient from '../services/apiClient';
import inspectionTemplateApi from '../services/inspectionTemplateApi';
import assetCategoryApi from '../services/assetCategoryApi';
import ReferenceDataLayout from '../components/layout/ReferenceDataLayout';
import PaginationControls from '../components/PaginationControls';
import { canManageInspectionTemplates, canViewInspectionTemplates } from '../constants/userRoles';
import { ROUTES } from '../constants/routes';
import {
  INSPECTION_TEMPLATE_STATUS_OPTIONS,
  getInspectionTemplateStatusLabel,
} from '../constants/inspectionTemplateStatuses';
import { getApiErrorMessage, isForbidden } from '../utils/apiError';
import { DEFAULT_PAGE, getPageNumber, getTotalPages, unwrapPageContent } from '../utils/pagination';

function formatTimestamp(timestamp) {
  if (!timestamp) {
    return '-';
  }
  return new Date(timestamp).toLocaleString();
}

function getQuestionsActionLabel(status) {
  return status === 'DRAFT' ? 'Manage Questions' : 'View Questions';
}

export default function InspectionTemplatesPage() {
  const navigate = useNavigate();
  const { auth } = useAuth();
  const [templates, setTemplates] = useState([]);
  const [categories, setCategories] = useState([]);
  const [templatesPage, setTemplatesPage] = useState(DEFAULT_PAGE);
  const [templatesTotalPages, setTemplatesTotalPages] = useState(0);
  const [listLoading, setListLoading] = useState(false);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [filterAssetCategoryId, setFilterAssetCategoryId] = useState('');
  const [filterStatus, setFilterStatus] = useState('');
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    assetCategoryId: '',
  });
  const [editingId, setEditingId] = useState(null);

  const canView = canViewInspectionTemplates(auth?.user?.role);
  const canManage = canManageInspectionTemplates(auth?.user?.role);

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
    assetCategoryId: filterAssetCategoryId ? Number(filterAssetCategoryId) : undefined,
    status: filterStatus || undefined,
  });

  const handleFilterChange = (e) => {
    const { name, value } = e.target;
    const nextCategory = name === 'filterAssetCategoryId' ? value : filterAssetCategoryId;
    const nextStatus = name === 'filterStatus' ? value : filterStatus;
    if (name === 'filterAssetCategoryId') {
      setFilterAssetCategoryId(value);
    } else if (name === 'filterStatus') {
      setFilterStatus(value);
    }
    loadTemplatesWithFilters(DEFAULT_PAGE, nextCategory, nextStatus);
  };

  const loadTemplatesWithFilters = async (page, categoryFilter, statusFilter) => {
    try {
      setListLoading(true);
      setError(null);
      const filters = {
        assetCategoryId: categoryFilter ? Number(categoryFilter) : undefined,
        status: statusFilter || undefined,
      };
      const templatePage = await inspectionTemplateApi.list(page, undefined, filters);
      setTemplates(unwrapPageContent(templatePage));
      setTemplatesPage(getPageNumber(templatePage, page));
      setTemplatesTotalPages(getTotalPages(templatePage));
    } catch (err) {
      setError(getApiErrorMessage(err, 'Failed to load inspection templates.'));
    } finally {
      setListLoading(false);
    }
  };

  const loadTemplates = async (page = templatesPage) => {
    await loadTemplatesWithFilters(page, filterAssetCategoryId, filterStatus);
  };

  const loadPageData = async (page = templatesPage) => {
    try {
      setLoading(true);
      setError(null);
      const [templatePage, categoryList] = await Promise.all([
        inspectionTemplateApi.list(page, undefined, buildFilters()),
        assetCategoryApi.list(),
      ]);
      setTemplates(unwrapPageContent(templatePage));
      setTemplatesPage(getPageNumber(templatePage, page));
      setTemplatesTotalPages(getTotalPages(templatePage));
      setCategories(categoryList);
    } catch (err) {
      setError(getApiErrorMessage(err, 'Failed to load inspection templates.'));
    } finally {
      setLoading(false);
      setListLoading(false);
    }
  };

  const handleFormChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const resetForm = () => {
    setFormData({ name: '', description: '', assetCategoryId: '' });
    setEditingId(null);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!canManage) return;

    try {
      setSubmitting(true);
      setError(null);
      setSuccess(null);
      const payload = {
        name: formData.name.trim(),
        description: formData.description.trim() || undefined,
        assetCategoryId: Number(formData.assetCategoryId),
      };
      if (editingId) {
        await inspectionTemplateApi.update(editingId, {
          name: payload.name,
          description: payload.description,
        });
        setSuccess('Inspection template updated successfully.');
      } else {
        await inspectionTemplateApi.create(payload);
        setSuccess('Inspection template created successfully.');
      }
      resetForm();
      await loadTemplates(templatesPage);
    } catch (err) {
      if (isForbidden(err)) {
        setError('You do not have permission to manage inspection templates.');
      } else {
        setError(getApiErrorMessage(err, 'Failed to save inspection template.'));
      }
    } finally {
      setSubmitting(false);
    }
  };

  const handleEdit = (template) => {
    if (template.status !== 'DRAFT') {
      return;
    }
    setEditingId(template.id);
    setFormData({
      name: template.name,
      description: template.description || '',
      assetCategoryId: String(template.assetCategoryId),
    });
  };

  const handlePublish = async (id) => {
    if (!window.confirm('Publish this inspection template?')) return;

    try {
      setError(null);
      setSuccess(null);
      await inspectionTemplateApi.publish(id);
      setSuccess('Inspection template published.');
      if (editingId === id) {
        resetForm();
      }
      await loadTemplates(templatesPage);
    } catch (err) {
      if (isForbidden(err)) {
        setError('You do not have permission to publish inspection templates.');
      } else {
        setError(getApiErrorMessage(err, 'Failed to publish inspection template.'));
      }
    }
  };

  const handleArchive = async (id) => {
    if (!window.confirm('Archive this inspection template?')) return;

    try {
      setError(null);
      setSuccess(null);
      await inspectionTemplateApi.archive(id);
      setSuccess('Inspection template archived.');
      if (editingId === id) {
        resetForm();
      }
      await loadTemplates(templatesPage);
    } catch (err) {
      if (isForbidden(err)) {
        setError('You do not have permission to archive inspection templates.');
      } else {
        setError(getApiErrorMessage(err, 'Failed to archive inspection template.'));
      }
    }
  };

  if (loading) {
    return <div className="loading">Loading inspection templates...</div>;
  }

  return (
    <ReferenceDataLayout title="Inspection Templates">
      {error && <div className="error-message">{error}</div>}
      {success && <div className="success-message">{success}</div>}

      {!canManage && (
        <p className="read-only-note">
          Inspection templates are read-only. Administrators can create, edit, and archive
          templates.
        </p>
      )}

      <section className="reference-form-section">
        <h2>Filters</h2>
        <div className="reference-form">
          <div className="form-row">
            <label htmlFor="filterAssetCategoryId">Asset Category</label>
            <select
              id="filterAssetCategoryId"
              name="filterAssetCategoryId"
              value={filterAssetCategoryId}
              onChange={handleFilterChange}
              disabled={listLoading}
            >
              <option value="">All categories</option>
              {categories.map((category) => (
                <option key={category.id} value={category.id}>
                  {category.name}
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
              {INSPECTION_TEMPLATE_STATUS_OPTIONS.map((option) => (
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
          <h2>{editingId ? 'Edit Inspection Template' : 'Create Inspection Template'}</h2>
          <form className="reference-form" onSubmit={handleSubmit}>
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
            <div className="form-row">
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
            <div className="form-row">
              <label htmlFor="assetCategoryId">Asset Category</label>
              <select
                id="assetCategoryId"
                name="assetCategoryId"
                value={formData.assetCategoryId}
                onChange={handleFormChange}
                required={!editingId}
                disabled={submitting || Boolean(editingId)}
              >
                <option value="">Select asset category</option>
                {categories.map((category) => (
                  <option key={category.id} value={category.id}>
                    {category.name}
                  </option>
                ))}
              </select>
            </div>
            <div className="form-actions">
              <button type="submit" className="btn-primary" disabled={submitting}>
                {submitting ? 'Saving...' : editingId ? 'Update Template' : 'Create Template'}
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
        <h2>Templates</h2>
        {templates.length === 0 ? (
          <p className="no-items">No inspection templates found.</p>
        ) : (
          <table className="reference-table">
            <thead>
              <tr>
                <th>Name</th>
                <th>Asset Category</th>
                <th>Version</th>
                <th>Status</th>
                <th>Updated</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {templates.map((template) => (
                <tr key={template.id}>
                  <td>{template.name}</td>
                  <td>{template.assetCategoryName}</td>
                  <td>{template.version}</td>
                  <td>{getInspectionTemplateStatusLabel(template.status)}</td>
                  <td>{formatTimestamp(template.updatedAt)}</td>
                  <td>
                    <button
                      type="button"
                      className="btn-link"
                      onClick={() => navigate(`/inspection-templates/${template.id}/questions`)}
                    >
                      {getQuestionsActionLabel(template.status)}
                    </button>
                    {canManage && template.status === 'DRAFT' && (
                      <>
                        {' '}
                        <button
                          type="button"
                          className="btn-link"
                          onClick={() => handleEdit(template)}
                        >
                          Edit
                        </button>{' '}
                        <button
                          type="button"
                          className="btn-link"
                          onClick={() => handlePublish(template.id)}
                        >
                          Publish
                        </button>
                      </>
                    )}
                    {canManage && template.status === 'PUBLISHED' && (
                      <>
                        {' '}
                        <button
                          type="button"
                          className="btn-link"
                          onClick={() => handleArchive(template.id)}
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
        page={templatesPage}
        totalPages={templatesTotalPages}
        loading={listLoading}
        onPrevious={() => loadTemplates(templatesPage - 1)}
        onNext={() => loadTemplates(templatesPage + 1)}
      />
    </ReferenceDataLayout>
  );
}
