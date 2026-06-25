import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import apiClient from '../services/apiClient';
import workOrderApi from '../services/workOrderApi';
import operationalDecisionApi from '../services/operationalDecisionApi';
import NotificationButton from '../components/NotificationButton';
import { canCreateWorkOrders } from '../constants/userRoles';
import { getOperationalDecisionOutcomeLabel } from '../constants/operationalDecisionOutcomes';
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
  const [decisions, setDecisions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [formData, setFormData] = useState({
    operationalDecisionId: '',
    description: '',
    priority: WORK_ORDER_PRIORITIES.NORMAL,
    createdAtBusinessDate: toDateTimeLocalValue(),
  });

  const canCreate = canCreateWorkOrders(auth?.user?.role);

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
      const [workOrderData, decisionData] = await Promise.all([
        workOrderApi.list(),
        operationalDecisionApi.list(),
      ]);
      setWorkOrders(workOrderData);
      setDecisions(decisionData);
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
                  <th>Created</th>
                </tr>
              </thead>
              <tbody>
                {workOrders.map((workOrder) => (
                  <tr key={workOrder.id}>
                    <td>{workOrder.assetName}</td>
                    <td>#{workOrder.operationalDecisionId}</td>
                    <td>{getOperationalDecisionOutcomeLabel(workOrder.workType)}</td>
                    <td>{workOrder.description}</td>
                    <td>{getWorkOrderPriorityLabel(workOrder.priority)}</td>
                    <td>{workOrder.status}</td>
                    <td>
                      {workOrder.createdAtBusinessDate
                        ? new Date(workOrder.createdAtBusinessDate).toLocaleString()
                        : '-'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </section>
      </main>
    </div>
  );
}
