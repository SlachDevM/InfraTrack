import { EXECUTION_CANDIDATE_STATUS_OPTIONS } from '../../constants/executionCandidateStatuses';

export default function PreventiveCandidateFilters({
  filterStatus,
  filterAssetId,
  filterPlanId,
  assets,
  plans,
  onFilterChange,
}) {
  return (
    <div className="filter-row">
      <label htmlFor="filterStatus">
        Status
        <select
          id="filterStatus"
          name="filterStatus"
          value={filterStatus}
          onChange={onFilterChange}
        >
          {EXECUTION_CANDIDATE_STATUS_OPTIONS.map((option) => (
            <option key={option.value || 'all'} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>
      </label>
      <label htmlFor="filterAssetId">
        Asset
        <select
          id="filterAssetId"
          name="filterAssetId"
          value={filterAssetId}
          onChange={onFilterChange}
        >
          <option value="">All assets</option>
          {assets.map((asset) => (
            <option key={asset.id} value={asset.id}>
              {asset.name}
            </option>
          ))}
        </select>
      </label>
      <label htmlFor="filterPlanId">
        Plan
        <select
          id="filterPlanId"
          name="filterPlanId"
          value={filterPlanId}
          onChange={onFilterChange}
        >
          <option value="">All plans</option>
          {plans.map((plan) => (
            <option key={plan.id} value={plan.id}>
              {plan.planCode} —{plan.name}
            </option>
          ))}
        </select>
      </label>
    </div>
  );
}
