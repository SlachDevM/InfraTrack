import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import apiClient from '../services/apiClient';
import departmentApi from '../services/departmentApi';
import ReferenceDataLayout from '../components/layout/ReferenceDataLayout';
import { canManageUsers } from '../constants/userRoles';
import { ROUTES } from '../constants/routes';
import { getApiErrorMessage } from '../utils/apiError';

export default function DepartmentsPage() {
  const navigate = useNavigate();
  const { auth } = useAuth();
  const [departments, setDepartments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [name, setName] = useState('');
  const [editingId, setEditingId] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  const isAdministrator = canManageUsers(auth?.user?.role);

  useEffect(() => {
    if (!auth) {
      navigate(ROUTES.LOGIN);
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

  if (loading) {
    return <div className="loading">Loading departments...</div>;
  }

  return (
    <ReferenceDataLayout title="Departments">
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
    </ReferenceDataLayout>
  );
}
