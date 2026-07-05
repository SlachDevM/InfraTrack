import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, waitFor, cleanup } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import ExportCsvButton from '../../components/ExportCsvButton';
import reportingExportApi from '../../services/reportingExportApi';
import { toExportDateRangeParams } from '../../utils/reportingExportDateRange';

const mockAuth = { token: 'test-token', user: { userId: 2, role: 'MANAGER' } };

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

describe('ExportCsvButton', () => {
  afterEach(() => {
    cleanup();
    vi.useRealTimers();
  });

  beforeEach(() => {
    vi.clearAllMocks();
    reportingExportApi.exportAssets.mockResolvedValue(undefined);
  });

  it('renders export label and calls assets endpoint with default date range', async () => {
    const user = userEvent.setup();
    const onError = vi.fn();
    const exportRange = { fromDate: '2026-02-13', toDate: '2026-03-15' };
    const exportParams = toExportDateRangeParams(exportRange);

    render(<ExportCsvButton exportType="assets" exportRange={exportRange} onError={onError} />);

    const button = screen.getByRole('button', { name: 'Export CSV' });
    expect(button).toHaveAttribute('aria-busy', 'false');

    await user.click(button);

    await waitFor(() => {
      expect(reportingExportApi.exportAssets).toHaveBeenCalledWith(
        'test-token',
        exportParams,
        'csv'
      );
    });
    expect(onError).not.toHaveBeenCalled();
  });

  it('uses custom label for accessible name', () => {
    render(<ExportCsvButton exportType="assets" label="Export assets CSV" onError={vi.fn()} />);

    expect(screen.getByRole('button', { name: 'Export assets CSV' })).toBeInTheDocument();
  });

  it('reports forbidden errors via onError', async () => {
    const user = userEvent.setup();
    const onError = vi.fn();
    const forbidden = new Error('Forbidden');
    forbidden.status = 403;
    reportingExportApi.exportAssets.mockRejectedValue(forbidden);

    render(
      <ExportCsvButton
        exportType="assets"
        exportRange={{ fromDate: '2026-01-01', toDate: '2026-01-31' }}
        onError={onError}
      />
    );
    await user.click(screen.getByRole('button', { name: 'Export CSV' }));

    await waitFor(() => {
      expect(onError).toHaveBeenCalledWith(
        'You do not have permission to export operational data.'
      );
    });
  });
});
