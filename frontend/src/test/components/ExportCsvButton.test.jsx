import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, waitFor, cleanup } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import ExportCsvButton from '../../components/ExportCsvButton';
import reportingExportApi from '../../services/reportingExportApi';

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
  afterEach(cleanup);

  beforeEach(() => {
    vi.clearAllMocks();
    reportingExportApi.exportAssets.mockResolvedValue(undefined);
  });

  it('renders export label and calls assets endpoint on click', async () => {
    const user = userEvent.setup();
    const onError = vi.fn();

    render(<ExportCsvButton exportType="assets" onError={onError} />);

    const button = screen.getByRole('button', { name: 'Export CSV' });
    expect(button).toHaveAttribute('aria-busy', 'false');

    await user.click(button);

    await waitFor(() => {
      expect(reportingExportApi.exportAssets).toHaveBeenCalledWith('test-token', undefined);
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

    render(<ExportCsvButton exportType="assets" onError={onError} />);
    await user.click(screen.getByRole('button', { name: 'Export CSV' }));

    await waitFor(() => {
      expect(onError).toHaveBeenCalledWith(
        'You do not have permission to export operational data.'
      );
    });
  });
});
