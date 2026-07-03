import { COMMON_LABELS } from './uiLabels';

export const REPORTING_EXPORT_FORMATS = {
  CSV: 'csv',
  XLSX: 'xlsx',
};

export const REPORTING_EXPORT_TYPES = {
  ASSETS: 'assets',
  INSPECTIONS: 'inspections',
  ISSUES: 'issues',
  WORK_ORDERS: 'workOrders',
  PREVENTIVE_CANDIDATES: 'preventiveCandidates',
};

const EXPORT_DEFINITIONS = {
  [REPORTING_EXPORT_TYPES.ASSETS]: {
    baseFilename: 'assets-export',
    endpointBase: '/api/reporting/exports/assets',
  },
  [REPORTING_EXPORT_TYPES.INSPECTIONS]: {
    baseFilename: 'inspections-export',
    endpointBase: '/api/reporting/exports/inspections',
  },
  [REPORTING_EXPORT_TYPES.ISSUES]: {
    baseFilename: 'issues-export',
    endpointBase: '/api/reporting/exports/issues',
  },
  [REPORTING_EXPORT_TYPES.WORK_ORDERS]: {
    baseFilename: 'work-orders-export',
    endpointBase: '/api/reporting/exports/work-orders',
  },
  [REPORTING_EXPORT_TYPES.PREVENTIVE_CANDIDATES]: {
    baseFilename: 'preventive-candidates-export',
    endpointBase: '/api/reporting/exports/preventive-candidates',
  },
};

function buildExportConfig(type, format) {
  const definition = EXPORT_DEFINITIONS[type];
  if (!definition) {
    return null;
  }
  const extension = format === REPORTING_EXPORT_FORMATS.XLSX ? '.xlsx' : '.csv';
  return {
    label:
      format === REPORTING_EXPORT_FORMATS.XLSX
        ? COMMON_LABELS.EXPORT_XLSX
        : COMMON_LABELS.EXPORT_CSV,
    filename: `${definition.baseFilename}${extension}`,
    endpoint: `${definition.endpointBase}${extension}`,
  };
}

export const REPORTING_EXPORTS = Object.fromEntries(
  Object.values(REPORTING_EXPORT_TYPES).map((type) => [
    type,
    buildExportConfig(type, REPORTING_EXPORT_FORMATS.CSV),
  ])
);

export function getReportingExportConfig(type, format = REPORTING_EXPORT_FORMATS.CSV) {
  return buildExportConfig(type, format);
}
