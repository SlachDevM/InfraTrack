import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import apiClient from '../services/apiClient';
import inspectionApi from '../services/inspectionApi';
import businessTriggerApi from '../services/businessTriggerApi';
import NotificationButton from '../components/NotificationButton';
import { canAssignInspections } from '../constants/userRoles';
import { getBusinessTriggerTypeLabel } from '../constants/businessTriggerTypes';
import {
  INSPECTION_PRIORITIES,
  INSPECTION_PRIORITY_OPTIONS,
  getInspectionPriorityLabel,
} from '../constants/inspectionPriorities';
import '../styles/ReferenceDataPage.css';
import '../styles/InspectionsPage.css';

export default function InspectionsPage() {
  const navigate = useNavigate();
  const { auth, logout } = useAuth();
  const [inspections, setInspections] = useState([]);
  const [triggers, setTriggers] = useState([]);
  const [workers, setWorkers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [formData, setFormData] = useState({
    businessTriggerId: '',
    assignedToUserId: '',
    priority: INSPECTION_PRIORITIES.NORMAL,
    expectedCompletionDate: '',
  });

  const canAssign = canAssignInspections(auth?.user?.role);

  const selectedTrigger = useMemo(
    () => triggers.find((trigger) => String(trigger.id) === String(formData.businessTriggerId)),
    [triggers, formData.businessTriggerId]
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
      const [inspectionData, triggerData] = await Promise.all([
        inspectionApi.list(),
        businessTriggerApi.list(),
      ]);
      setInspections(inspectionData);
      setTriggers(triggerData);

      if (canAssignInspections(auth?.user?.role)) {
        const workerData = await inspectionApi.listWorkers();
        setWorkers(workerData);
      }
    } catch (err) {
      setError(`Failed to load inspections: ${err.message}`);
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => {
      const next = { ...prev, [name]: value };
      if (name === 'businessTriggerId') {
        const trigger = triggers.find((item) => String(item.id) === String(value));
        if (trigger?.urgent) {
          next.priority = INSPECTION_PRIORITIES.URGENT;
        }
      }
      return next;
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!canAssign) return;

    try {
      setSubmitting(true);
      setError(null);
      setSuccess(null);
      const request = {
        businessTriggerId: Number(formData.businessTriggerId),
        assignedToUserId: Number(formData.assignedToUserId),
        priority: formData.priority,
      };
      if (formData.expectedCompletionDate) {
        request.expectedCompletionDate = formData.expectedCompletionDate;
      }
      await inspectionApi.assign(request);
      setSuccess('Inspection assigned successfully.');
      setFormData({
        businessTriggerId: '',
        assignedToUserId: '',
        priority: INSPECTION_PRIORITIES.NORMAL,
        expectedCompletionDate: '',
      });
      await loadPageData();
    } catch (err) {
      if (err.status === 403) {
        setError('You do not have permission to assign inspections.');
      } else {
        setError(`Failed to assign inspection: ${err.message}`);
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
    return <div className="loading">Loading inspections...</div>;
  }

  return (
    <div className="reference-data-page inspections-page">
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
        <h1>Inspections</h1>
        <div className="user-header-actions">
          <NotificationButton />
          <button type="button" className="logout-btn" onClick={handleLogout}>
            Logout
          </button>
        </div>
      </header>

      <main className="reference-content inspections-content">
        {error && <div className="error-message">{error}</div>}
        {success && <div className="success-message">{success}</div>}

        {canAssign ? (
          <section className="inspection-form-section">
            <h2>Assign Inspection</h2>
            <form className="inspection-form" onSubmit={handleSubmit}>
              <div className="form-row">
                <label htmlFor="businessTriggerId">Business Trigger</label>
                <select
                  id="businessTriggerId"
                  name="businessTriggerId"
                  value={formData.businessTriggerId}
                  onChange={handleChange}
                  required
                  disabled={submitting || triggers.length === 0}
                >
                  <option value="">Select business trigger</option>
                  {triggers.map((trigger) => (
                    <option key={trigger.id} value={trigger.id}>
                      #{trigger.id} — {trigger.assetName} ({getBusinessTriggerTypeLabel(trigger.type)})
                    </option>
                  ))}
                </select>
              </div>

              {selectedTrigger && (
                <div className="linked-asset-info">
                  <strong>Linked asset:</strong> {selectedTrigger.assetName}
                  <br />
                  <strong>Reason:</strong> {selectedTrigger.reason}
                  {selectedTrigger.urgent && (
                    <>
                      <br />
                      <strong>Urgent trigger</strong>
                    </>
                  )}
                </div>
              )}

              <div className="form-row">
                <label htmlFor="assignedToUserId">Assign To</label>
                <select
                  id="assignedToUserId"
                  name="assignedToUserId"
                  value={formData.assignedToUserId}
                  onChange={handleChange}
                  required
                  disabled={submitting || workers.length === 0}
                >
                  <option value="">Select worker</option>
                  {workers.map((worker) => (
                    <option key={worker.id} value={worker.id}>
                      {worker.name} ({worker.role === 'CONTRACTOR' ? 'Contractor' : 'Field Employee'})
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
                  onChange={handleChange}
                  required
                  disabled={submitting}
                >
                  {INSPECTION_PRIORITY_OPTIONS.map((option) => (
                    <option key={option.value} value={option.value}>
                      {option.label}
                    </option>
                  ))}
                </select>
              </div>

              <div className="form-row">
                <label htmlFor="expectedCompletionDate">Expected Completion Date</label>
                <input
                  id="expectedCompletionDate"
                  name="expectedCompletionDate"
                  type="date"
                  value={formData.expectedCompletionDate}
                  onChange={handleChange}
                  disabled={submitting}
                />
              </div>

              <button
                type="submit"
                className="btn-primary"
                disabled={submitting || triggers.length === 0 || workers.length === 0}
              >
                {submitting ? 'Assigning...' : 'Assign Inspection'}
              </button>
            </form>
            {triggers.length === 0 && (
              <p className="read-only-note">Create at least one business trigger before assigning an inspection.</p>
            )}
            {workers.length === 0 && (
              <p className="read-only-note">No field employees or contractors are available for assignment.</p>
            )}
          </section>
        ) : (
          <p className="read-only-note">
            Inspection assignment is available to Operational Coordinators.
          </p>
        )}

        <section className="inspection-list-section">
          <h2>Assigned Inspections</h2>
          {inspections.length === 0 ? (
            <p className="no-items">No inspections assigned yet.</p>
          ) : (
            <table className="reference-table inspections-table">
              <thead>
                <tr>
                  <th>Asset</th>
                  <th>Trigger</th>
                  <th>Assigned To</th>
                  <th>Priority</th>
                  <th>Status</th>
                  <th>Expected By</th>
                  <th>Created</th>
                </tr>
              </thead>
              <tbody>
                {inspections.map((inspection) => (
                  <tr key={inspection.id}>
                    <td>{inspection.assetName}</td>
                    <td>
                      #{inspection.businessTriggerId} — {getBusinessTriggerTypeLabel(inspection.businessTriggerType)}
                    </td>
                    <td>{inspection.assignedToUserName || inspection.assignedToUserId}</td>
                    <td>{getInspectionPriorityLabel(inspection.priority)}</td>
                    <td>{inspection.status}</td>
                    <td>{inspection.expectedCompletionDate || '-'}</td>
                    <td>
                      {inspection.createdAt
                        ? new Date(inspection.createdAt).toLocaleString()
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
