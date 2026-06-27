import { useState, useEffect } from 'react';
import userApi from '../services/userApi';
import departmentApi from '../services/departmentApi';
import { getRoleLabel } from '../constants/userRoles';
import { getApiErrorMessage } from '../utils/apiError';

export default function EditUserModal({ isOpen, onClose, onSuccess, user }) {
  const [departments, setDepartments] = useState([]);
  const [formData, setFormData] = useState({
    name: user?.name || '',
    email: user?.email || '',
    departmentId: user?.departmentId ? String(user.departmentId) : '',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!isOpen) return;

    setFormData({
      name: user?.name || '',
      email: user?.email || '',
      departmentId: user?.departmentId ? String(user.departmentId) : '',
    });

    departmentApi
      .list()
      .then(setDepartments)
      .catch(() => setDepartments([]));
  }, [isOpen, user]);

  if (!isOpen || !user) return null;

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      setLoading(true);
      setError(null);

      const payload = {
        name: formData.name,
        email: formData.email,
      };

      if (formData.departmentId) {
        payload.departmentId = Number(formData.departmentId);
      } else if (user.departmentId) {
        payload.clearDepartment = true;
      }

      const result = await userApi.updateUser(user.id, payload);
      onSuccess(result);
    } catch (err) {
      setError(getApiErrorMessage(err, 'Failed to update user'));
      setLoading(false);
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <h2>Edit User</h2>
        {error && <div className="error-message">{error}</div>}
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="name">Name</label>
            <input
              id="name"
              type="text"
              name="name"
              value={formData.name}
              onChange={handleChange}
              required
              disabled={loading}
            />
          </div>

          <div className="form-group">
            <label htmlFor="email">Email</label>
            <input
              id="email"
              type="email"
              name="email"
              value={formData.email}
              onChange={handleChange}
              required
              disabled={loading}
            />
          </div>

          <div className="form-group">
            <label htmlFor="departmentId">Department</label>
            <select
              id="departmentId"
              name="departmentId"
              value={formData.departmentId}
              onChange={handleChange}
              disabled={loading}
            >
              <option value="">No department</option>
              {departments.map((department) => (
                <option key={department.id} value={department.id}>
                  {department.name}
                </option>
              ))}
            </select>
          </div>

          <div className="form-group readonly">
            <label>Role</label>
            <input
              type="text"
              value={getRoleLabel(user.role)}
              disabled
              className="readonly-field"
            />
            <small>Role cannot be changed here</small>
          </div>

          <div className="modal-actions">
            <button
              type="button"
              className="btn-cancel"
              onClick={onClose}
              disabled={loading}
            >
              Cancel
            </button>
            <button
              type="submit"
              className="btn-primary"
              disabled={loading}
            >
              {loading ? 'Saving...' : 'Save Changes'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
