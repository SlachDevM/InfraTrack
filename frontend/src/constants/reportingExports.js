import { COMMON_LABELS } from './uiLabels';

export const REPORTING_EXPORT_TYPES = {
  ASSETS: 'assets',
  INSPECTIONS: 'inspections',
  ISSUES: 'issues',
  WORK_ORDERS: 'workOrders',
  PREVENTIVE_CANDIDATES: 'preventiveCandidates',
};

export const REPORTING_EXPORTS = {
  [REPORTING_EXPORT_TYPES.ASSETS]: {
    label: COMMON_LABELS.EXPORT_CSV,
    filename: 'assets-export.csv',
    endpoint: '/api/reporting/exports/assets.csv',
  },
  [REPORTING_EXPORT_TYPES.INSPECTIONS]: {
    label: COMMON_LABELS.EXPORT_CSV,
    filename: 'inspections-export.csv',
    endpoint: '/api/reporting/exports/inspections.csv',
  },
  [REPORTING_EXPORT_TYPES.ISSUES]: {
    label: COMMON_LABELS.EXPORT_CSV,
    filename: 'issues-export.csv',
    endpoint: '/api/reporting/exports/issues.csv',
  },
  [REPORTING_EXPORT_TYPES.WORK_ORDERS]: {
    label: COMMON_LABELS.EXPORT_CSV,
    filename: 'work-orders-export.csv',
    endpoint: '/api/reporting/exports/work-orders.csv',
  },
  [REPORTING_EXPORT_TYPES.PREVENTIVE_CANDIDATES]: {
    label: COMMON_LABELS.EXPORT_CSV,
    filename: 'preventive-candidates-export.csv',
    endpoint: '/api/reporting/exports/preventive-candidates.csv',
  },
};
