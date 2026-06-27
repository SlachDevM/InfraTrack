import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import apiClient from '../services/apiClient';
import businessTriggerApi from '../services/businessTriggerApi';
import assetApi from '../services/assetApi';
import userApi from '../services/userApi';
import NotificationButton from '../components/NotificationButton';
import PaginationControls from '../components/PaginationControls';
import { canCreateBusinessTriggers } from '../constants/userRoles';
import { getApiErrorMessage, isForbidden } from '../utils/apiError';
import {
  DEFAULT_PAGE,
  MAX_PAGE_SIZE,
  getPageNumber,
  getTotalPages,
  unwrapPageContent,
} from '../utils/pagination';
import {
  BUSINESS_TRIGGER_TYPES,
  BUSINESS_TRIGGER_TYPE_OPTIONS,
  getBusinessTriggerTypeLabel,
} from '../constants/businessTriggerTypes';
import '../styles/ReferenceDataPage.css';
import '../styles/BusinessTriggersPage.css';

export default function BusinessTriggersPage() {
  const navigate = useNavigate();
  const { auth, logout } = useAuth();
  const [triggers, setTriggers] = useState([]);
  const [triggersPage, setTriggersPage] = useState(DEFAULT_PAGE);
  const [triggersTotalPages, setTriggersTotalPages] = useState(0);
  const [listLoading, setListLoading] = useState(false);
  const [assets, setAssets] = useState([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [formData, setFormData] = useState({
    assetId: '',
    type: BUSINESS_TRIGGER_TYPES.CUSTOMER_REQUEST,
    reason: '',
    urgent: false,
  });

  const canCreate = canCreateBusinessTriggers(auth?.user?.role);

  useEffect(() => {
    if (!auth) {
      navigate('/login');
      return;
    }
    apiClient.setToken(auth.token);
    loadPageData();
  }, [auth, navigate]);

  const loadPageData = async (page = triggersPage) => {
    try {
      setLoading(true);
      setError(null);
      const [triggerPage, assetPage] = await Promise.all([
        businessTriggerApi.list(page),
        assetApi.list(0, MAX_PAGE_SIZE),
      ]);
      setTriggers(unwrapPageContent(triggerPage));
      setTriggersPage(getPageNumber(triggerPage, page));
      setTriggersTotalPages(getTotalPages(triggerPage));
      let loadedAssets = unwrapPageContent(assetPage);
      if (canCreate) {
        const profile = await userApi.getCurrentUser();
        if (profile?.departmentId != null) {
          loadedAssets = loadedAssets.filter(
            (asset) => asset.departmentId === profile.departmentId
          );
        }
      }
      setAssets(loadedAssets);
    } catch (err) {
      setError(getApiErrorMessage(err, 'Failed to load business triggers.'));
    } finally {
      setLoading(false);
      setListLoading(false);
    }
  };

  const loadTriggers = async (page = triggersPage) => {
    try {
      setListLoading(true);
      setError(null);
      const triggerPage = await businessTriggerApi.list(page);
      setTriggers(unwrapPageContent(triggerPage));
      setTriggersPage(getPageNumber(triggerPage, page));
      setTriggersTotalPages(getTotalPages(triggerPage));
    } catch (err) {
      setError(getApiErrorMessage(err, 'Failed to load business triggers.'));
    } finally {
      setListLoading(false);
    }
  };

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!canCreate) return;

    try {
      setSubmitting(true);
      setError(null);
      setSuccess(null);
      await businessTriggerApi.create({
        assetId: Number(formData.assetId),
        type: formData.type,
        reason: formData.reason,
        urgent: formData.type === BUSINESS_TRIGGER_TYPES.EMERGENCY_EVENT || formData.urgent,
      });
      setSuccess('Business trigger created successfully.');
      setFormData({
        assetId: '',
        type: BUSINESS_TRIGGER_TYPES.CUSTOMER_REQUEST,
        reason: '',
        urgent: false,
      });
      await loadPageData(triggersPage);
    } catch (err) {
      if (isForbidden(err)) {
        setError(getApiErrorMessage(err, 'You do not have permission to create business triggers.'));
      } else {
        setError(getApiErrorMessage(err, 'Failed to create business trigger.'));
      }
    } finally {
      setSubmitting(false);
    }
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  if (loading) {
    return <div className="loading">Loading business triggers...</div>;
  }

  return (
    <div className="reference-data-page business-triggers-page">
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
        <h1>Business Triggers</h1>
        <div className="user-header-actions">
          <NotificationButton />
          <button type="button" className="logout-btn" onClick={handleLogout}>
            Logout
          </button>
        </div>
      </header>

      <main className="reference-content business-triggers-content">
        {error && <div className="error-message">{error}</div>}
        {success && <div className="success-message">{success}</div>}

        {canCreate ? (
          <section className="trigger-form-section">
            <h2>Create Business Trigger</h2>
            <form className="trigger-form" onSubmit={handleSubmit}>
              <div className="form-row">
                <label htmlFor="assetId">Asset</label>
                <select
                  id="assetId"
                  name="assetId"
                  value={formData.assetId}
                  onChange={handleChange}
                  required
                  disabled={submitting || assets.length === 0}
                >
                  <option value="">Select asset</option>
                  {assets.map((asset) => (
                    <option key={asset.id} value={asset.id}>
                      {asset.name} ({asset.departmentName})
                    </option>
                  ))}
                </select>
              </div>

              <div className="form-row">
                <label htmlFor="type">Trigger Type</label>
                <select
                  id="type"
                  name="type"
                  value={formData.type}
                  onChange={handleChange}
                  required
                  disabled={submitting}
                >
                  {BUSINESS_TRIGGER_TYPE_OPTIONS.map((option) => (
                    <option key={option.value} value={option.value}>
                      {option.label}
                    </option>
                  ))}
                </select>
              </div>

              <div className="form-row">
                <label htmlFor="reason">Reason</label>
                <textarea
                  id="reason"
                  name="reason"
                  value={formData.reason}
                  onChange={handleChange}
                  required
                  disabled={submitting}
                  rows={3}
                />
              </div>

              <div className="form-row checkbox-row">
                <label htmlFor="urgent">
                  <input
                    id="urgent"
                    name="urgent"
                    type="checkbox"
                    checked={
                      formData.type === BUSINESS_TRIGGER_TYPES.EMERGENCY_EVENT || formData.urgent
                    }
                    onChange={handleChange}
                    disabled={
                      submitting || formData.type === BUSINESS_TRIGGER_TYPES.EMERGENCY_EVENT
                    }
                  />
                  Urgent
                </label>
              </div>

              <button
                type="submit"
                className="btn-primary"
                disabled={submitting || assets.length === 0}
              >
                {submitting ? 'Creating...' : 'Create Business Trigger'}
              </button>
            </form>
            {assets.length === 0 && (
              <p className="read-only-note">Register at least one asset before creating a trigger.</p>
            )}
          </section>
        ) : (
          <p className="read-only-note">
            Business trigger creation is available to Managers and Operational Coordinators.
          </p>
        )}

        <section className="trigger-list-section">
          <h2>Business Triggers</h2>
          {triggers.length === 0 ? (
            <p className="no-items">No business triggers yet.</p>
          ) : (
            <table className="reference-table triggers-table">
              <thead>
                <tr>
                  <th>Asset</th>
                  <th>Type</th>
                  <th>Reason</th>
                  <th>Urgent</th>
                  <th>Created</th>
                </tr>
              </thead>
              <tbody>
                {triggers.map((trigger) => (
                  <tr key={trigger.id}>
                    <td>{trigger.assetName}</td>
                    <td>{getBusinessTriggerTypeLabel(trigger.type)}</td>
                    <td>{trigger.reason}</td>
                    <td>{trigger.urgent ? 'Yes' : 'No'}</td>
                    <td>
                      {trigger.createdAt
                        ? new Date(trigger.createdAt).toLocaleString()
                        : '-'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </section>
        <PaginationControls
          page={triggersPage}
          totalPages={triggersTotalPages}
          loading={listLoading}
          onPrevious={() => loadTriggers(triggersPage - 1)}
          onNext={() => loadTriggers(triggersPage + 1)}
        />
      </main>
    </div>
  );
}
