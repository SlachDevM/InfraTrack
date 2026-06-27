import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import apiClient from '../services/apiClient';
import delegatedAuthorityApi from '../services/delegatedAuthorityApi';
import departmentApi from '../services/departmentApi';
import userApi from '../services/userApi';
import NotificationButton from '../components/NotificationButton';
import { canManageDelegatedAuthority } from '../constants/userRoles';
import { getApiErrorMessage } from '../utils/apiError';
import '../styles/ReferenceDataPage.css';

function toDateTimeLocalValue(date = new Date()) {
  const pad = (value) => String(value).padStart(2, '0');
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
}

export default function DelegatedAuthoritiesPage() {
  const navigate = useNavigate();
  const { auth, logout } = useAuth();
  const [authorities, setAuthorities] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [managers, setManagers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);
  const [formData, setFormData] = useState({
    delegateManagerUserId: '',
    sourceDepartmentId: '',
    targetDepartmentId: '',
    reason: '',
    validFrom: toDateTimeLocalValue(),
    validUntil: toDateTimeLocalValue(new Date(Date.now() + 7 * 24 * 60 * 60 * 1000)),
  });

  const canManage = canManageDelegatedAuthority(auth?.user?.role);

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
      const [authorityData, departmentData] = await Promise.all([
        delegatedAuthorityApi.list(),
        departmentApi.list(),
      ]);
      setAuthorities(authorityData);
      setDepartments(departmentData);

      if (canManageDelegatedAuthority(auth.user.role)) {
        const managerData = await userApi.getManagers();
        setManagers(managerData);
      }
    } catch (err) {
      setError(getApiErrorMessage(err, 'Failed to load delegated authorities.'));
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
    if (!canManage) return;

    try {
      setSubmitting(true);
      setError(null);
      await delegatedAuthorityApi.create({
        delegateManagerUserId: Number(formData.delegateManagerUserId),
        sourceDepartmentId: Number(formData.sourceDepartmentId),
        targetDepartmentId: Number(formData.targetDepartmentId),
        reason: formData.reason,
        validFrom: `${formData.validFrom}:00`,
        validUntil: `${formData.validUntil}:00`,
      });
      setFormData({
        delegateManagerUserId: '',
        sourceDepartmentId: '',
        targetDepartmentId: '',
        reason: '',
        validFrom: toDateTimeLocalValue(),
        validUntil: toDateTimeLocalValue(new Date(Date.now() + 7 * 24 * 60 * 60 * 1000)),
      });
      await loadPageData();
    } catch (err) {
      setError(getApiErrorMessage(err, 'Failed to create delegation.'));
    } finally {
      setSubmitting(false);
    }
  };

  const handleRevoke = async (id) => {
    if (!window.confirm('Revoke this delegated authority?')) return;

    try {
      setError(null);
      await delegatedAuthorityApi.revoke(id);
      await loadPageData();
    } catch (err) {
      setError(getApiErrorMessage(err, 'Failed to revoke delegation.'));
    }
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  if (loading) {
    return <div className="loading">Loading delegated authorities...</div>;
  }

  return (
    <div className="reference-data-page">
      <header
        className="reference-header"
        style={{
          background: 'linear-gradient(135deg, #1a472a 0%, #2d6b4d 100%)',
          color: 'white',
        }}
      >
        <button type="button" className="back-btn" onClick={() => navigate('/')}>
          ← Back to Dashboard
        </button>
        <h1>Delegated Authority</h1>
        <div className="reference-header-actions">
          <NotificationButton />
          <button type="button" className="logout-btn" onClick={handleLogout}>
            Logout
          </button>
        </div>
      </header>

      {error && <div className="error-message">{error}</div>}

      <main className="reference-content">
        {canManage ? (
          <section className="reference-form-section">
            <h2>Create Delegation</h2>
            <form onSubmit={handleSubmit} className="reference-form">
              <div className="form-group">
                <label htmlFor="delegateManagerUserId">Delegate Manager</label>
                <select
                  id="delegateManagerUserId"
                  name="delegateManagerUserId"
                  value={formData.delegateManagerUserId}
                  onChange={handleChange}
                  required
                  disabled={submitting}
                >
                  <option value="">Select manager</option>
                  {managers.map((manager) => (
                    <option key={manager.id} value={manager.id}>
                      {manager.name} ({manager.email})
                    </option>
                  ))}
                </select>
              </div>

              <div className="form-group">
                <label htmlFor="sourceDepartmentId">Source Department</label>
                <select
                  id="sourceDepartmentId"
                  name="sourceDepartmentId"
                  value={formData.sourceDepartmentId}
                  onChange={handleChange}
                  required
                  disabled={submitting}
                >
                  <option value="">Select department</option>
                  {departments.map((department) => (
                    <option key={department.id} value={department.id}>
                      {department.name}
                    </option>
                  ))}
                </select>
              </div>

              <div className="form-group">
                <label htmlFor="targetDepartmentId">Target Department</label>
                <select
                  id="targetDepartmentId"
                  name="targetDepartmentId"
                  value={formData.targetDepartmentId}
                  onChange={handleChange}
                  required
                  disabled={submitting}
                >
                  <option value="">Select department</option>
                  {departments.map((department) => (
                    <option key={department.id} value={department.id}>
                      {department.name}
                    </option>
                  ))}
                </select>
              </div>

              <div className="form-group">
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

              <div className="form-group">
                <label htmlFor="validFrom">Valid From</label>
                <input
                  id="validFrom"
                  type="datetime-local"
                  name="validFrom"
                  value={formData.validFrom}
                  onChange={handleChange}
                  required
                  disabled={submitting}
                />
              </div>

              <div className="form-group">
                <label htmlFor="validUntil">Valid Until</label>
                <input
                  id="validUntil"
                  type="datetime-local"
                  name="validUntil"
                  value={formData.validUntil}
                  onChange={handleChange}
                  required
                  disabled={submitting}
                />
              </div>

              <button type="submit" className="btn-primary" disabled={submitting}>
                {submitting ? 'Creating...' : 'Create Delegation'}
              </button>
            </form>
          </section>
        ) : (
          <p className="read-only-note">Delegated authority management is available to managers only.</p>
        )}

        <section className="reference-list-section">
          <h2>Delegations</h2>
          {authorities.length === 0 ? (
            <p>No delegated authorities recorded.</p>
          ) : (
            <table className="reference-table">
              <thead>
                <tr>
                  <th>Delegate</th>
                  <th>Source</th>
                  <th>Target</th>
                  <th>Valid From</th>
                  <th>Valid Until</th>
                  <th>Status</th>
                  <th>Reason</th>
                  {canManage && <th>Actions</th>}
                </tr>
              </thead>
              <tbody>
                {authorities.map((authority) => (
                  <tr key={authority.id}>
                    <td>{authority.delegateManagerUserId}</td>
                    <td>{authority.sourceDepartmentName}</td>
                    <td>{authority.targetDepartmentName}</td>
                    <td>{new Date(authority.validFrom).toLocaleString()}</td>
                    <td>{new Date(authority.validUntil).toLocaleString()}</td>
                    <td>{authority.revoked ? 'Revoked' : 'Active'}</td>
                    <td>{authority.reason}</td>
                    {canManage && (
                      <td>
                        {!authority.revoked && (
                          <button
                            type="button"
                            className="action-btn deactivate-btn"
                            onClick={() => handleRevoke(authority.id)}
                          >
                            Revoke
                          </button>
                        )}
                      </td>
                    )}
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
