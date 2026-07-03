import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, waitFor, cleanup } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import ExportReportingMenu from '../../components/ExportReportingMenu';
import reportingExportApi from '../../services/reportingExportApi';
import { REPORTING_EXPORT_FORMATS } from '../../constants/reportingExports';
import { canExportReporting, USER_ROLES } from '../../constants/userRoles';

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

function renderExportMenu(props = {}) {
  return render(
    <ExportReportingMenu exportType="assets" onError={vi.fn()} {...props} />
  );
}

describe('ExportReportingMenu', () => {
  afterEach(cleanup);

  beforeEach(() => {
    vi.clearAllMocks();
    reportingExportApi.exportAssets.mockResolvedValue(undefined);
  });

  it('renders a single Export button instead of separate format buttons', () => {
    renderExportMenu();

    expect(screen.getByRole('button', { name: 'Export' })).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Export CSV' })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Export XLSX' })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Export PDF' })).not.toBeInTheDocument();
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
    expect(screen.getByRole('button', { name: 'Export' })).toHaveAttribute('aria-haspopup', 'menu');
  });

  it('calls the CSV endpoint when Export as CSV is selected', async () => {
    const user = userEvent.setup();
    const onError = vi.fn();
    renderExportMenu({ onError });

    await user.click(screen.getByRole('button', { name: 'Export' }));
    await user.click(screen.getByRole('menuitem', { name: 'Export as CSV' }));

    await waitFor(() => {
      expect(reportingExportApi.exportAssets).toHaveBeenCalledWith(
        'test-token',
        undefined,
        REPORTING_EXPORT_FORMATS.CSV
      );
    });
    expect(onError).not.toHaveBeenCalled();
  });

  it('calls the XLSX endpoint when Export as XLSX is selected', async () => {
    const user = userEvent.setup();
    renderExportMenu();

    await user.click(screen.getByRole('button', { name: 'Export' }));
    await user.click(screen.getByRole('menuitem', { name: 'Export as XLSX' }));

    await waitFor(() => {
      expect(reportingExportApi.exportAssets).toHaveBeenCalledWith(
        'test-token',
        undefined,
        REPORTING_EXPORT_FORMATS.XLSX
      );
    });
  });

  it('calls the PDF endpoint when Export as PDF is selected', async () => {
    const user = userEvent.setup();
    renderExportMenu();

    await user.click(screen.getByRole('button', { name: 'Export' }));
    await user.click(screen.getByRole('menuitem', { name: 'Export as PDF' }));

    await waitFor(() => {
      expect(reportingExportApi.exportAssets).toHaveBeenCalledWith(
        'test-token',
        undefined,
        REPORTING_EXPORT_FORMATS.PDF
      );
    });
  });

  it('closes the menu after a format is selected', async () => {
    const user = userEvent.setup();
    renderExportMenu();

    await user.click(screen.getByRole('button', { name: 'Export' }));
    await user.click(screen.getByRole('menuitem', { name: 'Export as CSV' }));

    await waitFor(() => {
      expect(screen.queryByRole('menuitem', { name: 'Export as CSV' })).not.toBeInTheDocument();
    });
    expect(screen.getByRole('button', { name: 'Export' })).toHaveAttribute('aria-expanded', 'false');
  });

  it('closes the menu when Escape is pressed', async () => {
    const user = userEvent.setup();
    renderExportMenu();

    await user.click(screen.getByRole('button', { name: 'Export' }));
    expect(screen.getByRole('menuitem', { name: 'Export as CSV' })).toBeInTheDocument();

    await user.keyboard('{Escape}');

    expect(screen.queryByRole('menuitem', { name: 'Export as CSV' })).not.toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Export' })).toHaveAttribute('aria-expanded', 'false');
  });

  it('closes the menu on outside click', async () => {
    const user = userEvent.setup();
    render(
      <div>
        <button type="button">Outside</button>
        <ExportReportingMenu exportType="assets" onError={vi.fn()} />
      </div>
    );

    await user.click(screen.getByRole('button', { name: 'Export' }));
    expect(screen.getByRole('menuitem', { name: 'Export as CSV' })).toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: 'Outside' }));

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
