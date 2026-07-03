import { PREVENTIVE_MAINTENANCE_PLAN_STATUS_OPTIONS } from '../../constants/preventiveMaintenancePlanStatuses';
import { PLAN_TRIGGER_TYPE_OPTIONS } from '../../constants/planTriggerTypes';

export default function PreventivePlanFilters({
  filterAssetId,
  filterStatus,
  filterTriggerType,
  assets,
  listLoading,
  onFilterChange,
}) {
  return (
    <section className="reference-form-section">
      <h2>Filters</h2>
      <div className="filter-row">
        <div className="form-row">
          <label htmlFor="filterAssetId">Asset</label>
          <select
            id="filterAssetId"
            name="filterAssetId"
            value={filterAssetId}
            onChange={onFilterChange}
            disabled={listLoading}
          >
            <option value="">All assets</option>
            {assets.map((asset) => (
              <option key={asset.id} value={asset.id}>
                {asset.name}
              </option>
            ))}
          </select>
        </div>
        <div className="form-row">
          <label htmlFor="filterStatus">Status</label>
          <select
            id="filterStatus"
            name="filterStatus"
            value={filterStatus}
            onChange={onFilterChange}
            disabled={listLoading}
          >
            <option value="">All statuses</option>
            {PREVENTIVE_MAINTENANCE_PLAN_STATUS_OPTIONS.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </div>
        <div className="form-row">
          <label htmlFor="filterTriggerType">Trigger Type</label>
          <select
            id="filterTriggerType"
            name="filterTriggerType"
            value={filterTriggerType}
            onChange={onFilterChange}
            disabled={listLoading}
          >
            <option value="">All trigger types</option>
            {PLAN_TRIGGER_TYPE_OPTIONS.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </div>
      </div>
    </section>
  );
}
