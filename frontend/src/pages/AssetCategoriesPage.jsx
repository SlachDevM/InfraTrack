import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import apiClient from '../services/apiClient';
import assetCategoryApi from '../services/assetCategoryApi';
import ReferenceDataLayout from '../components/layout/ReferenceDataLayout';
import { canManageUsers } from '../constants/userRoles';
import { ROUTES } from '../constants/routes';
import { getApiErrorMessage } from '../utils/apiError';

export default function AssetCategoriesPage() {
  const navigate = useNavigate();
  const { auth } = useAuth();
  const [categories, setCategories] = useState([]);
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
    loadCategories();
  }, [auth, navigate]);

  const loadCategories = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await assetCategoryApi.list();
      setCategories(data);
    } catch (err) {
      setError(getApiErrorMessage(err, 'Failed to load asset categories.'));
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
        await assetCategoryApi.update(editingId, { name });
      } else {
        await assetCategoryApi.create({ name });
      }
      setName('');
      setEditingId(null);
      await loadCategories();
    } catch (err) {
      setError(getApiErrorMessage(err, 'Failed to save asset category.'));
    } finally {
      setSubmitting(false);
    }
  };

  const handleEdit = (category) => {
    setEditingId(category.id);
    setName(category.name);
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this asset category?')) return;

    try {
      setError(null);
      await assetCategoryApi.delete(id);
      await loadCategories();
    } catch (err) {
      setError(getApiErrorMessage(err, 'Failed to delete asset category.'));
    }
  };

  if (loading) {
    return <div className="loading">Loading asset categories...</div>;
  }

  return (
    <ReferenceDataLayout title="Asset Categories">
      {error && <div className="error-message">{error}</div>}

      {!isAdministrator && (
        <p className="read-only-note">
          Reference data is read-only. Administrators can create and manage asset categories.
        </p>
      )}

      {isAdministrator && (
        <form className="reference-form" onSubmit={handleSubmit}>
          <input
            type="text"
            placeholder={editingId ? 'Edit category name' : 'New category name'}
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

      {categories.length === 0 ? (
        <p className="no-items">No asset categories yet.</p>
      ) : (
        <table className="reference-table">
          <thead>
            <tr>
              <th>Name</th>
              {isAdministrator && <th>Actions</th>}
            </tr>
          </thead>
          <tbody>
            {categories.map((category) => (
              <tr key={category.id}>
                <td>{category.name}</td>
                {isAdministrator && (
                  <td className="actions-cell">
                    <button
                      type="button"
                      className="action-btn edit-btn"
                      onClick={() => handleEdit(category)}
                    >
                      Edit
                    </button>
                    <button
                      type="button"
                      className="action-btn delete-btn"
                      onClick={() => handleDelete(category.id)}
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
