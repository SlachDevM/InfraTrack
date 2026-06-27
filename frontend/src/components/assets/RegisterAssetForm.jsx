import { ASSET_STATUS_OPTIONS } from '../../constants/assetStatuses';

export default function RegisterAssetForm({
  formData,
  departments,
  categories,
  submitting,
  onChange,
  onSubmit,
}) {
  return (
    <section className="asset-form-section">
      <h2>Register Asset</h2>
      <form className="asset-form" onSubmit={onSubmit}>
        <div className="form-row">
          <label htmlFor="name">Asset name</label>
          <input
            id="name"
            name="name"
            type="text"
            value={formData.name}
            onChange={onChange}
            required
            disabled={submitting}
          />
        </div>

        <div className="form-row">
          <label htmlFor="departmentId">Department</label>
          <select
            id="departmentId"
            name="departmentId"
            value={formData.departmentId}
            onChange={onChange}
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

        <div className="form-row">
          <label htmlFor="assetCategoryId">Asset Category</label>
          <select
            id="assetCategoryId"
            name="assetCategoryId"
            value={formData.assetCategoryId}
            onChange={onChange}
            required
            disabled={submitting}
          >
            <option value="">Select category</option>
            {categories.map((category) => (
              <option key={category.id} value={category.id}>
                {category.name}
              </option>
            ))}
          </select>
        </div>

        <div className="form-row">
          <label htmlFor="location">Location</label>
          <input
            id="location"
            name="location"
            type="text"
            value={formData.location}
            onChange={onChange}
            required
            disabled={submitting}
          />
        </div>

        <div className="form-row">
          <label htmlFor="status">Status</label>
          <select
            id="status"
            name="status"
            value={formData.status}
            onChange={onChange}
            required
            disabled={submitting}
          >
            {ASSET_STATUS_OPTIONS.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </div>

        <div className="form-row">
          <label htmlFor="registrationDate">Registration Date</label>
          <input
            id="registrationDate"
            name="registrationDate"
            type="date"
            value={formData.registrationDate}
            onChange={onChange}
            required
            disabled={submitting}
          />
        </div>

        <button type="submit" className="btn-primary" disabled={submitting}>
          {submitting ? 'Registering...' : 'Register Asset'}
        </button>
      </form>
    </section>
  );
}
