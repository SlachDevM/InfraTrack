export const MAX_REPORTING_EXPORT_WINDOW_DAYS = 365;
export const DEFAULT_REPORTING_EXPORT_RANGE_DAYS = 30;

export const REPORTING_EXPORT_RANGE_MESSAGES = {
  REQUIRED: 'Reporting exports require both from and to date filters.',
  TO_BEFORE_FROM: 'Reporting export to date must not be before from date.',
  WINDOW_EXCEEDED: 'Reporting exports cannot span more than 365 days.',
};

const DAY_MS = 24 * 60 * 60 * 1000;

export function toLocalDateInputValue(date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

export function startOfLocalDay(dateInputValue) {
  if (!dateInputValue) {
    return null;
  }
  const [year, month, day] = dateInputValue.split('-').map(Number);
  return new Date(year, month - 1, day, 0, 0, 0, 0).getTime();
}

export function endOfLocalDay(dateInputValue) {
  if (!dateInputValue) {
    return null;
  }
  const [year, month, day] = dateInputValue.split('-').map(Number);
  return new Date(year, month - 1, day, 23, 59, 59, 999).getTime();
}

export function createDefaultReportingExportRange(referenceDate = new Date()) {
  const today = new Date(referenceDate);
  const toDate = toLocalDateInputValue(today);
  const fromDateObj = new Date(today);
  fromDateObj.setHours(0, 0, 0, 0);
  fromDateObj.setDate(fromDateObj.getDate() - DEFAULT_REPORTING_EXPORT_RANGE_DAYS);
  return {
    fromDate: toLocalDateInputValue(fromDateObj),
    toDate,
  };
}

export function countInclusiveLocalDays(fromDate, toDate) {
  const fromMs = startOfLocalDay(fromDate);
  const toMs = startOfLocalDay(toDate);
  if (fromMs == null || toMs == null) {
    return 0;
  }
  return Math.floor((toMs - fromMs) / DAY_MS) + 1;
}

export function validateReportingExportRange({ fromDate, toDate }) {
  if (!fromDate || !toDate) {
    return REPORTING_EXPORT_RANGE_MESSAGES.REQUIRED;
  }
  const fromMs = startOfLocalDay(fromDate);
  const toMs = endOfLocalDay(toDate);
  if (toMs < fromMs) {
    return REPORTING_EXPORT_RANGE_MESSAGES.TO_BEFORE_FROM;
  }
  if (countInclusiveLocalDays(fromDate, toDate) > MAX_REPORTING_EXPORT_WINDOW_DAYS) {
    return REPORTING_EXPORT_RANGE_MESSAGES.WINDOW_EXCEEDED;
  }
  return null;
}

export function toExportDateRangeParams({ fromDate, toDate }) {
  return {
    from: startOfLocalDay(fromDate),
    to: endOfLocalDay(toDate),
  };
}

export function resolveReportingExportParams(exportRange) {
  const range = exportRange ?? createDefaultReportingExportRange();
  const validationError = validateReportingExportRange(range);
  if (validationError) {
    throw new Error(validationError);
  }
  return toExportDateRangeParams(range);
}
