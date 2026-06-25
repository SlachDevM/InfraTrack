export const ASSET_STATUSES = {
  ACTIVE: 'ACTIVE',
  LIMITED_SERVICE: 'LIMITED_SERVICE',
  OUT_OF_SERVICE: 'OUT_OF_SERVICE',
  DECOMMISSIONED: 'DECOMMISSIONED',
};

export const ASSET_STATUS_LABELS = {
  [ASSET_STATUSES.ACTIVE]: 'Active',
  [ASSET_STATUSES.LIMITED_SERVICE]: 'Limited Service',
  [ASSET_STATUSES.OUT_OF_SERVICE]: 'Out of Service',
  [ASSET_STATUSES.DECOMMISSIONED]: 'Decommissioned',
};

export const ASSET_STATUS_OPTIONS = Object.entries(ASSET_STATUS_LABELS).map(
  ([value, label]) => ({ value, label })
);

export function getAssetStatusLabel(status) {
  return ASSET_STATUS_LABELS[status] || status;
}
