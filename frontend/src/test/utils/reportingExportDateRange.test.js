import { describe, it, expect, vi, afterEach } from 'vitest';
import {
  MAX_REPORTING_EXPORT_WINDOW_DAYS,
  REPORTING_EXPORT_RANGE_MESSAGES,
  countInclusiveLocalDays,
  createDefaultReportingExportRange,
  endOfLocalDay,
  startOfLocalDay,
  toExportDateRangeParams,
  toLocalDateInputValue,
  validateReportingExportRange,
} from '../../utils/reportingExportDateRange';

describe('reportingExportDateRange', () => {
  afterEach(() => {
    vi.useRealTimers();
  });

  it('creates a default last-30-days inclusive range', () => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date(2026, 2, 15, 12, 0, 0));

    expect(createDefaultReportingExportRange()).toEqual({
      fromDate: '2026-02-13',
      toDate: '2026-03-15',
    });
  });

  it('serializes export range params as epoch millis', () => {
    const params = toExportDateRangeParams({
      fromDate: '2026-01-01',
      toDate: '2026-01-31',
    });

    expect(params.from).toBe(startOfLocalDay('2026-01-01'));
    expect(params.to).toBe(endOfLocalDay('2026-01-31'));
  });

  it('rejects missing from or to dates', () => {
    expect(validateReportingExportRange({ fromDate: '', toDate: '2026-01-01' })).toBe(
      REPORTING_EXPORT_RANGE_MESSAGES.REQUIRED
    );
    expect(validateReportingExportRange({ fromDate: '2026-01-01', toDate: '' })).toBe(
      REPORTING_EXPORT_RANGE_MESSAGES.REQUIRED
    );
  });

  it('rejects to before from', () => {
    expect(validateReportingExportRange({ fromDate: '2026-02-01', toDate: '2026-01-01' })).toBe(
      REPORTING_EXPORT_RANGE_MESSAGES.TO_BEFORE_FROM
    );
  });

  it('allows same-day and 365-day ranges', () => {
    expect(
      validateReportingExportRange({ fromDate: '2026-01-01', toDate: '2026-01-01' })
    ).toBeNull();
    expect(
      validateReportingExportRange({ fromDate: '2026-01-01', toDate: '2026-12-31' })
    ).toBeNull();
    expect(countInclusiveLocalDays('2026-01-01', '2026-12-31')).toBe(
      MAX_REPORTING_EXPORT_WINDOW_DAYS
    );
  });

  it('rejects ranges over 365 days', () => {
    expect(validateReportingExportRange({ fromDate: '2025-01-01', toDate: '2026-01-02' })).toBe(
      REPORTING_EXPORT_RANGE_MESSAGES.WINDOW_EXCEEDED
    );
  });

  it('formats local date input values consistently', () => {
    expect(toLocalDateInputValue(new Date(2026, 0, 5))).toBe('2026-01-05');
  });
});
