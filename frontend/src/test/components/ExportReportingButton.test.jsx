import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, waitFor, cleanup } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import ExportReportingButton from '../../components/ExportReportingButton';
import reportingExportApi from '../../services/reportingExportApi';
import { REPORTING_EXPORT_FORMATS } from '../../constants/reportingExports';
import { toExportDateRangeParams } from '../../utils/reportingExportDateRange';

const mockAuth = { token: 'test-token', user: { userId: 2, role: 'MANAGER' } };
const exportRange = { fromDate: '2026-01-01', toDate: '2026-01-31' };
const exportParams = toExportDateRangeParams(exportRange);

vi.mock('../../context/AuthContext', () => ({
  useAuth: () => ({ auth: mockAuth }),
}));

vi.mock('../../services/reportingExportApi', () => ({
  default: {
    exportAssets: vi.fn(),
    exportInspections: vi.fn(),
    exportIssues: vi.fn(),
    exportWorkOrders: vi.fn(),
    exportPreventiveCandidates: vi.fn(),
  },
}));

describe('ExportReportingButton', () => {
  afterEach(cleanup);

  beforeEach(() => {
    vi.clearAllMocks();
    reportingExportApi.exportAssets.mockResolvedValue(undefined);
  });

  it('calls assets XLSX endpoint with export range params', async () => {
    const user = userEvent.setup();
    const onError = vi.fn();

    render(
      <ExportReportingButton
        exportType="assets"
        format={REPORTING_EXPORT_FORMATS.XLSX}
        exportRange={exportRange}
        onError={onError}
      />
    );

    await user.click(screen.getByRole('button', { name: 'Export XLSX' }));

    await waitFor(() => {
      expect(reportingExportApi.exportAssets).toHaveBeenCalledWith(
        'test-token',
        exportParams,
        REPORTING_EXPORT_FORMATS.XLSX
      );
    });
    expect(onError).not.toHaveBeenCalled();
  });

  it('calls assets PDF endpoint with export range params', async () => {
    const user = userEvent.setup();
    const onError = vi.fn();

    render(
      <ExportReportingButton
        exportType="assets"
        format={REPORTING_EXPORT_FORMATS.PDF}
        exportRange={exportRange}
        onError={onError}
      />
    );

    await user.click(screen.getByRole('button', { name: 'Export PDF' }));

    await waitFor(() => {
      expect(reportingExportApi.exportAssets).toHaveBeenCalledWith(
        'test-token',
        exportParams,
        REPORTING_EXPORT_FORMATS.PDF
      );
    });
    expect(onError).not.toHaveBeenCalled();
  });

  it('reports forbidden errors via onError', async () => {
    const user = userEvent.setup();
    const onError = vi.fn();
    const forbidden = new Error('Forbidden');
    forbidden.status = 403;
    reportingExportApi.exportAssets.mockRejectedValue(forbidden);

    render(
      <ExportReportingButton
        exportType="assets"
        format={REPORTING_EXPORT_FORMATS.XLSX}
        exportRange={exportRange}
        onError={onError}
      />
    );
    await user.click(screen.getByRole('button', { name: 'Export XLSX' }));

    await waitFor(() => {
      expect(onError).toHaveBeenCalledWith(
        'You do not have permission to export operational data.'
      );
    });
  });
});
