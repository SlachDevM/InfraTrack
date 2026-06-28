export const INSPECTION_TEMPLATE_STATUSES = {
  DRAFT: 'DRAFT',
  PUBLISHED: 'PUBLISHED',
  ARCHIVED: 'ARCHIVED',
};

export const INSPECTION_TEMPLATE_STATUS_LABELS = {
  [INSPECTION_TEMPLATE_STATUSES.DRAFT]: 'Draft',
  [INSPECTION_TEMPLATE_STATUSES.PUBLISHED]: 'Published',
  [INSPECTION_TEMPLATE_STATUSES.ARCHIVED]: 'Archived',
};

export const INSPECTION_TEMPLATE_STATUS_OPTIONS = Object.entries(
  INSPECTION_TEMPLATE_STATUS_LABELS
).map(([value, label]) => ({ value, label }));

export function getInspectionTemplateStatusLabel(status) {
  return INSPECTION_TEMPLATE_STATUS_LABELS[status] || status;
}
