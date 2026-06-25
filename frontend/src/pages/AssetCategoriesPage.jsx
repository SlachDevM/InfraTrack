import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import apiClient from '../services/apiClient';
import assetCategoryApi from '../services/assetCategoryApi';
import NotificationButton from '../components/NotificationButton';
import { canManageUsers } from '../constants/userRoles';
import '../styles/ReferenceDataPage.css';

export default function AssetCategoriesPage() {
  const navigate = useNavigate();
  const { auth, logout } = useAuth();
  const [categories, setCategories] = useState([]);
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
    loadCategories();
  }, [auth, navigate]);

  const loadCategories = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await assetCategoryApi.list();
      setCategories(data);
    } catch (err) {
      setError(`Failed to load asset categories: ${err.message}`);
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
      setError(`Failed to save asset category: ${err.message}`);
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
      setError(`Failed to delete asset category: ${err.message}`);
    }
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  if (loading) {
    return <div className="loading">Loading asset categories...</div>;
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
        <h1>Asset Categories</h1>
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
      </main>
    </div>
  );
}
