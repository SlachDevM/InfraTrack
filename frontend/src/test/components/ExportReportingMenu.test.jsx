import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, waitFor, cleanup } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import ExportReportingMenu from '../../components/ExportReportingMenu';
import reportingExportApi from '../../services/reportingExportApi';
import { REPORTING_EXPORT_FORMATS } from '../../constants/reportingExports';
import { canExportReporting, USER_ROLES } from '../../constants/userRoles';
import {
  REPORTING_EXPORT_RANGE_MESSAGES,
  toExportDateRangeParams,
} from '../../utils/reportingExportDateRange';

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

function renderExportMenu(props = {}) {
  return render(
    <ExportReportingMenu
      exportType="assets"
      exportRange={exportRange}
      onError={vi.fn()}
      {...props}
    />
  );
}

describe('ExportReportingMenu', () => {
  afterEach(() => {
    cleanup();
    vi.useRealTimers();
  });

  beforeEach(() => {
    vi.clearAllMocks();
    reportingExportApi.exportAssets.mockResolvedValue(undefined);
  });

  it('renders export range inputs and a single Export button', () => {
    renderExportMenu();

    expect(screen.getByText('Export range:')).toBeInTheDocument();
    expect(screen.getByLabelText('Export from date')).toHaveValue('2026-01-01');
    expect(screen.getByLabelText('Export to date')).toHaveValue('2026-01-31');
    expect(screen.getByRole('button', { name: 'Export' })).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Export CSV' })).not.toBeInTheDocument();
  });

  it('opens the format menu on click', async () => {
    const user = userEvent.setup();
    renderExportMenu();

    expect(screen.queryByRole('menuitem', { name: 'Export as CSV' })).not.toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: 'Export' }));

    expect(screen.getByRole('menuitem', { name: 'Export as CSV' })).toBeInTheDocument();
    expect(screen.getByRole('menuitem', { name: 'Export as XLSX' })).toBeInTheDocument();
    expect(screen.getByRole('menuitem', { name: 'Export as PDF' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Export' })).toHaveAttribute('aria-expanded', 'true');
  });

  it('calls the CSV endpoint with from and to params', async () => {
    const user = userEvent.setup();
    const onError = vi.fn();
    renderExportMenu({ onError });

    await user.click(screen.getByRole('button', { name: 'Export' }));
    await user.click(screen.getByRole('menuitem', { name: 'Export as CSV' }));

    await waitFor(() => {
      expect(reportingExportApi.exportAssets).toHaveBeenCalledWith(
        'test-token',
        exportParams,
        REPORTING_EXPORT_FORMATS.CSV
      );
    });
    expect(onError).not.toHaveBeenCalled();
  });

  it('calls the XLSX endpoint with from and to params', async () => {
    const user = userEvent.setup();
    renderExportMenu();

    await user.click(screen.getByRole('button', { name: 'Export' }));
    await user.click(screen.getByRole('menuitem', { name: 'Export as XLSX' }));

    await waitFor(() => {
      expect(reportingExportApi.exportAssets).toHaveBeenCalledWith(
        'test-token',
        exportParams,
        REPORTING_EXPORT_FORMATS.XLSX
      );
    });
  });

  it('calls the PDF endpoint with from and to params', async () => {
    const user = userEvent.setup();
    renderExportMenu();

    await user.click(screen.getByRole('button', { name: 'Export' }));
    await user.click(screen.getByRole('menuitem', { name: 'Export as PDF' }));

    await waitFor(() => {
      expect(reportingExportApi.exportAssets).toHaveBeenCalledWith(
        'test-token',
        exportParams,
        REPORTING_EXPORT_FORMATS.PDF
      );
    });
  });

  it('generates the default last-30-days range when no exportRange prop is provided', () => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date(2026, 2, 15, 12, 0, 0));

    render(<ExportReportingMenu exportType="assets" onError={vi.fn()} />);

    expect(screen.getByLabelText('Export from date')).toHaveValue('2026-02-13');
    expect(screen.getByLabelText('Export to date')).toHaveValue('2026-03-15');
  });

  it('shows a validation error and does not call the API for an invalid range', async () => {
    const user = userEvent.setup();
    const onError = vi.fn();
    renderExportMenu({ onError });

    await user.clear(screen.getByLabelText('Export from date'));
    await user.click(screen.getByRole('button', { name: 'Export' }));
    await user.click(screen.getByRole('menuitem', { name: 'Export as CSV' }));

    await waitFor(() => {
      expect(onError).toHaveBeenCalledWith(REPORTING_EXPORT_RANGE_MESSAGES.REQUIRED);
    });
    expect(reportingExportApi.exportAssets).not.toHaveBeenCalled();
  });

  it('shows an error for ranges over 365 days without calling the API', async () => {
    const user = userEvent.setup();
    const onError = vi.fn();
    renderExportMenu({ onError });

    await user.clear(screen.getByLabelText('Export from date'));
    await user.type(screen.getByLabelText('Export from date'), '2024-01-01');
    await user.click(screen.getByRole('button', { name: 'Export' }));
    await user.click(screen.getByRole('menuitem', { name: 'Export as PDF' }));

    await waitFor(() => {
      expect(onError).toHaveBeenCalledWith(REPORTING_EXPORT_RANGE_MESSAGES.WINDOW_EXCEEDED);
    });
    expect(reportingExportApi.exportAssets).not.toHaveBeenCalled();
  });

  it('closes the menu after a format is selected', async () => {
    const user = userEvent.setup();
    renderExportMenu();

    await user.click(screen.getByRole('button', { name: 'Export' }));
    await user.click(screen.getByRole('menuitem', { name: 'Export as CSV' }));

    await waitFor(() => {
      expect(screen.queryByRole('menuitem', { name: 'Export as CSV' })).not.toBeInTheDocument();
    });
    expect(screen.getByRole('button', { name: 'Export' })).toHaveAttribute(
      'aria-expanded',
      'false'
    );
  });

  it('closes the menu when Escape is pressed', async () => {
    const user = userEvent.setup();
    renderExportMenu();

    await user.click(screen.getByRole('button', { name: 'Export' }));
    expect(screen.getByRole('menuitem', { name: 'Export as CSV' })).toBeInTheDocument();

    await user.keyboard('{Escape}');

    expect(screen.queryByRole('menuitem', { name: 'Export as CSV' })).not.toBeInTheDocument();
  });

  it('reports forbidden errors via onError', async () => {
    const user = userEvent.setup();
    const onError = vi.fn();
    const forbidden = new Error('Forbidden');
    forbidden.status = 403;
    reportingExportApi.exportAssets.mockRejectedValue(forbidden);

    renderExportMenu({ onError });

    await user.click(screen.getByRole('button', { name: 'Export' }));
    await user.click(screen.getByRole('menuitem', { name: 'Export as XLSX' }));

    await waitFor(() => {
      expect(onError).toHaveBeenCalledWith(
        'You do not have permission to export operational data.'
      );
    });
  });

  it('is hidden for field employees when the page does not render it', () => {
    const canExport = canExportReporting(USER_ROLES.FIELD_EMPLOYEE);
    expect(canExport).toBe(false);

    render(<>{canExport && <ExportReportingMenu exportType="assets" onError={vi.fn()} />}</>);

    expect(screen.queryByRole('button', { name: 'Export' })).not.toBeInTheDocument();
  });

  it('is hidden for contractors when the page does not render it', () => {
    const canExport = canExportReporting(USER_ROLES.CONTRACTOR);
    expect(canExport).toBe(false);

    render(<>{canExport && <ExportReportingMenu exportType="assets" onError={vi.fn()} />}</>);

    expect(screen.queryByRole('button', { name: 'Export' })).not.toBeInTheDocument();
  });
});
