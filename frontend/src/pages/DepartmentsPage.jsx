import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import apiClient from '../services/apiClient';
import departmentApi from '../services/departmentApi';
import NotificationButton from '../components/NotificationButton';
import { canManageUsers } from '../constants/userRoles';
import { getApiErrorMessage } from '../utils/apiError';
import '../styles/ReferenceDataPage.css';

export default function DepartmentsPage() {
  const navigate = useNavigate();
  const { auth, logout } = useAuth();
  const [departments, setDepartments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [name, setName] = useState('');
  const [editingId, setEditingId] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  const isAdministrator = canManageUsers(auth?.user?.role);

  useEffect(() => {
    if (!auth) {
      navigate('/login');
      return;
    }
    apiClient.setToken(auth.token);
    loadDepartments();
  }, [auth, navigate]);

  const loadDepartments = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await departmentApi.list();
      setDepartments(data);
    } catch (err) {
      setError(getApiErrorMessage(err, 'Failed to load departments.'));
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!isAdministrator) return;

    try {
      setSubmitting(true);
      setError(null);
      if (editingId) {
        await departmentApi.update(editingId, { name });
      } else {
        await departmentApi.create({ name });
      }
      setName('');
      setEditingId(null);
      await loadDepartments();
    } catch (err) {
      setError(getApiErrorMessage(err, 'Failed to save department.'));
    } finally {
      setSubmitting(false);
    }
  };

  const handleEdit = (department) => {
    setEditingId(department.id);
    setName(department.name);
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this department?')) return;

    try {
      setError(null);
      await departmentApi.delete(id);
      await loadDepartments();
    } catch (err) {
      setError(getApiErrorMessage(err, 'Failed to delete department.'));
    }
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  if (loading) {
    return <div className="loading">Loading departments...</div>;
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
          ← Back
        </button>
        <h1>Departments</h1>
        <div className="user-header-actions">
          <NotificationButton />
          <button type="button" className="logout-btn" onClick={handleLogout}>
            Logout
          </button>
        </div>
      </header>

      <main className="reference-content">
        {error && <div className="error-message">{error}</div>}

        {!isAdministrator && (
          <p className="read-only-note">
            Reference data is read-only. Administrators can create and manage departments.
          </p>
        )}

        {isAdministrator && (
          <form className="reference-form" onSubmit={handleSubmit}>
            <input
              type="text"
              placeholder={editingId ? 'Edit department name' : 'New department name'}
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
              disabled={submitting}
            />
            <button type="submit" className="btn-primary" disabled={submitting}>
              {editingId ? 'Update' : 'Add'}
            </button>
            {editingId && (
              <button
                type="button"
                className="btn-cancel"
                onClick={() => {
                  setEditingId(null);
                  setName('');
                }}
              >
                Cancel
              </button>
            )}
          </form>
        )}

        {departments.length === 0 ? (
          <p className="no-items">No departments yet.</p>
        ) : (
          <table className="reference-table">
            <thead>
              <tr>
                <th>Name</th>
                {isAdministrator && <th>Actions</th>}
              </tr>
            </thead>
            <tbody>
              {departments.map((department) => (
                <tr key={department.id}>
                  <td>{department.name}</td>
                  {isAdministrator && (
                    <td className="actions-cell">
                      <button
                        type="button"
                        className="action-btn edit-btn"
                        onClick={() => handleEdit(department)}
                      >
                        Edit
                      </button>
                      <button
                        type="button"
                        className="action-btn delete-btn"
                        onClick={() => handleDelete(department.id)}
                      >
                        Delete
                      </button>
                    </td>
                  )}
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </main>
    </div>
  );
}
