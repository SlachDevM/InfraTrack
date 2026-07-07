import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, waitFor, cleanup, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import PreventiveExecutionCandidatesPage from '../../pages/PreventiveExecutionCandidatesPage';
import preventiveExecutionCandidateApi from '../../services/preventiveExecutionCandidateApi';
import preventiveMaintenancePlanApi from '../../services/preventiveMaintenancePlanApi';
import assetApi from '../../services/assetApi';
import inspectionApi from '../../services/inspectionApi';
import userApi from '../../services/userApi';
import { USER_ROLES } from '../../constants/userRoles';
import { formatTimestamp } from '../../utils/dateTime';

const mockNavigate = vi.fn();

const { mockAuth, mockLogout } = vi.hoisted(() => ({
  mockAuth: { token: 'test-token', user: { userId: 1, role: 'ADMINISTRATOR' } },
  mockLogout: vi.fn(),
}));

vi.mock('../../services/preventiveExecutionCandidateApi', () => ({
  default: {
    list: vi.fn(),
    get: vi.fn(),
    getReport: vi.fn(),
    generate: vi.fn(),
    approve: vi.fn(),
    reject: vi.fn(),
    dismiss: vi.fn(),
  },
}));

vi.mock('../../services/inspectionApi', () => ({
  default: {
    listWorkers: vi.fn(),
  },
}));

vi.mock('../../services/userApi', () => ({
  default: {
    getCurrentUser: vi.fn(),
  },
}));

vi.mock('../../services/preventiveMaintenancePlanApi', () => ({
  default: {
    list: vi.fn(),
  },
}));

vi.mock('../../services/assetApi', () => ({
  default: {
    list: vi.fn(),
  },
}));

vi.mock('../../services/apiClient', () => ({
  default: {
    setToken: vi.fn(),
  },
}));

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

vi.mock('../../context/AuthContext', () => ({
  useAuth: () => ({
    auth: mockAuth,
    logout: mockLogout,
  }),
}));

vi.mock('../../components/NotificationButton', () => ({
  default: () => <button type="button">Notifications</button>,
}));

function pageResponse(content, number = 0, totalPages = 1) {
  return { content, number, totalPages };
}

const report = {
  id: 900,
  candidateId: 500,
  planCodeSnapshot: 'PUMP_MONTHLY',
  assetNameSnapshot: 'Pump A',
  decisionSource: 'PREVENTIVE_ENGINE',
  reportStatus: 'GENERATED',
  generatedAt: 1710000000000,
  approvedAt: null,
  rejectedAt: null,
  dismissedAt: null,
  inspectionCreatedAt: null,
  createdInspectionId: null,
  decisionReason: null,
};

const approvedReport = {
  ...report,
  reportStatus: 'INSPECTION_CREATED',
  approvedAt: 1710001000000,
  inspectionCreatedAt: 1710001000000,
  createdInspectionId: 900,
};

const rejectedReport = {
  ...report,
  reportStatus: 'REJECTED',
  rejectedAt: 1710001000000,
  decisionReason: 'Already inspected',
};

const dismissedReport = {
  ...report,
  reportStatus: 'DISMISSED',
  dismissedAt: 1710001000000,
  decisionReason: 'Not relevant',
};

const candidate = {
  id: 500,
  planId: 100,
  assetId: 5,
  assetName: 'Pump A',
  triggerType: 'TIME',
  candidateStatus: 'PENDING',
  eligibilityReason: 'One full month has elapsed.',
  evaluatedAt: 1710000000000,
  nextEligibleAt: null,
  planCodeSnapshot: 'PUMP_MONTHLY',
  planVersionSnapshot: 1,
  planNameSnapshot: 'Monthly Pump Inspection',
  targetActionSnapshot: 'CREATE_INSPECTION',
  triggerSummaryTitleSnapshot: 'Every month',
  triggerSummaryDescriptionSnapshot: 'Eligible once every full month from plan creation.',
  createdAt: 1710000000000,
  updatedAt: 1710000000000,
};

const assets = [{ id: 5, name: 'Pump A', departmentId: 10 }];
const plans = [{ id: 100, planCode: 'PUMP_MONTHLY', name: 'Monthly Pump Inspection' }];
const workers = [
  { id: 60, name: 'Field Worker', role: 'FIELD_EMPLOYEE', status: 'ACTIVE', departmentId: 10 },
];

describe('PreventiveExecutionCandidatesPage', () => {
  afterEach(cleanup);

  beforeEach(() => {
    vi.clearAllMocks();
    mockAuth.user.role = USER_ROLES.ADMINISTRATOR;
    preventiveExecutionCandidateApi.list.mockResolvedValue(pageResponse([candidate]));
    preventiveExecutionCandidateApi.get.mockResolvedValue(candidate);
    preventiveExecutionCandidateApi.getReport.mockResolvedValue(report);
    assetApi.list.mockResolvedValue(pageResponse(assets));
    preventiveMaintenancePlanApi.list.mockResolvedValue(pageResponse(plans));
    inspectionApi.listWorkers.mockResolvedValue(workers);
    userApi.getCurrentUser.mockResolvedValue({ departmentId: 10 });
  });

  it('renders candidate list for administrator', async () => {
    const { container } = render(
      <MemoryRouter>
        <PreventiveExecutionCandidatesPage />
      </MemoryRouter>
    );

    expect(await screen.findByText('PUMP_MONTHLY')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Generate Candidates' })).toBeInTheDocument();
    expect(container.querySelector('.reference-header')).toBeInTheDocument();
    expect(container.querySelector('.reference-content')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: '← Back' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Notifications' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Log out' })).toBeInTheDocument();
  });

  it('shows Page 1 of 1 when candidate list is empty', async () => {
    preventiveExecutionCandidateApi.list.mockResolvedValue(pageResponse([], 0, 0));

    render(
      <MemoryRouter>
        <PreventiveExecutionCandidatesPage />
      </MemoryRouter>
    );

    expect(
      await screen.findByText('No execution candidates match the current filters.')
    ).toBeInTheDocument();
    expect(screen.getByText('Page 1 of 1')).toBeInTheDocument();
    expect(screen.queryByText(/NaN/)).not.toBeInTheDocument();
  });

  it('shows Generate Candidates for manager', async () => {
    mockAuth.user.role = USER_ROLES.MANAGER;

    render(
      <MemoryRouter>
        <PreventiveExecutionCandidatesPage />
      </MemoryRouter>
    );

    expect(await screen.findByText('PUMP_MONTHLY')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Generate Candidates' })).toBeInTheDocument();
    expect(screen.getAllByRole('button', { name: 'Approve' }).length).toBeGreaterThan(0);
  });

  it('hides Generate Candidates for operational coordinator', async () => {
    mockAuth.user.role = USER_ROLES.OPERATIONAL_COORDINATOR;

    render(
      <MemoryRouter>
        <PreventiveExecutionCandidatesPage />
      </MemoryRouter>
    );

    expect(await screen.findByText('PUMP_MONTHLY')).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Generate Candidates' })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Approve' })).not.toBeInTheDocument();
  });

  it('redirects field employee away from page', async () => {
    mockAuth.user.role = USER_ROLES.FIELD_EMPLOYEE;

    render(
      <MemoryRouter>
        <PreventiveExecutionCandidatesPage />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith('/');
    });
  });

  it('generates candidates when button clicked', async () => {
    const user = userEvent.setup();
    preventiveExecutionCandidateApi.generate.mockResolvedValue([
      { planId: 100, planCode: 'PUMP_MONTHLY', outcome: 'CREATED', candidate },
    ]);

    render(
      <MemoryRouter>
        <PreventiveExecutionCandidatesPage />
      </MemoryRouter>
    );

    await screen.findByText('PUMP_MONTHLY');
    await user.click(screen.getByRole('button', { name: 'Generate Candidates' }));

    await waitFor(() => {
      expect(preventiveExecutionCandidateApi.generate).toHaveBeenCalled();
    });
    expect(await screen.findByText(/Generation complete/i)).toBeInTheDocument();
  });

  it('shows candidate detail without execute actions', async () => {
    const user = userEvent.setup();

    render(
      <MemoryRouter>
        <PreventiveExecutionCandidatesPage />
      </MemoryRouter>
    );

    await screen.findByText('PUMP_MONTHLY');
    await user.click(screen.getByRole('button', { name: 'View' }));

    await waitFor(() => {
      expect(preventiveExecutionCandidateApi.get).toHaveBeenCalledWith(500);
      expect(preventiveExecutionCandidateApi.getReport).toHaveBeenCalledWith(500);
    });
    expect(await screen.findByText('Candidate Detail')).toBeInTheDocument();
    expect(screen.getByText('One full month has elapsed.')).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Execute' })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Create Inspection' })).not.toBeInTheDocument();
  });

  it('renders candidate detail header with aligned close button and styled tabs', async () => {
    const user = userEvent.setup();

    const { container } = render(
      <MemoryRouter>
        <PreventiveExecutionCandidatesPage />
      </MemoryRouter>
    );

    await screen.findByText('PUMP_MONTHLY');
    await user.click(screen.getByRole('button', { name: 'View' }));
    await screen.findByText('Candidate Detail');

    const header = container.querySelector('.detail-panel-header');
    expect(header).toBeInTheDocument();
    expect(within(header).getByRole('heading', { name: 'Candidate Detail' })).toBeInTheDocument();
    expect(within(header).getByRole('button', { name: 'Close' })).toBeInTheDocument();

    const candidateTab = screen.getByRole('tab', { name: 'Candidate' });
    const reportTab = screen.getByRole('tab', { name: 'Execution Report' });
    expect(candidateTab).toHaveClass('detail-tab-active');
    expect(reportTab).not.toHaveClass('detail-tab-active');
    expect(candidateTab).toHaveAttribute('aria-selected', 'true');
    expect(reportTab).toHaveAttribute('aria-selected', 'false');
  });

  it('switches candidate detail tabs and updates active styling', async () => {
    const user = userEvent.setup();

    render(
      <MemoryRouter>
        <PreventiveExecutionCandidatesPage />
      </MemoryRouter>
    );

    await screen.findByText('PUMP_MONTHLY');
    await user.click(screen.getByRole('button', { name: 'View' }));
    await screen.findByText('Candidate Detail');

    const candidateTab = screen.getByRole('tab', { name: 'Candidate' });
    const reportTab = screen.getByRole('tab', { name: 'Execution Report' });

    await user.click(reportTab);
    expect(reportTab).toHaveClass('detail-tab-active');
    expect(candidateTab).not.toHaveClass('detail-tab-active');
    expect(reportTab).toHaveAttribute('aria-selected', 'true');
    expect(candidateTab).toHaveAttribute('aria-selected', 'false');

    const reportDetail = screen.getByText('Report Status').closest('dl');
    expect(within(reportDetail).getByText('Generated')).toBeInTheDocument();

    await user.click(candidateTab);
    expect(candidateTab).toHaveClass('detail-tab-active');
    expect(screen.getByText('One full month has elapsed.')).toBeInTheDocument();
  });

  it('shows execution report summary with generated status', async () => {
    const user = userEvent.setup();

    render(
      <MemoryRouter>
        <PreventiveExecutionCandidatesPage />
      </MemoryRouter>
    );

    await screen.findByText('PUMP_MONTHLY');
    await user.click(screen.getByRole('button', { name: 'View' }));
    await screen.findByText('Candidate Detail');
    await user.click(screen.getByRole('tab', { name: 'Execution Report' }));

    const reportDetail = screen.getByText('Report Status').closest('dl');
    expect(within(reportDetail).getByText('Generated')).toBeInTheDocument();
    expect(within(reportDetail).getByText('Preventive engine')).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Save Report' })).not.toBeInTheDocument();
  });

  it('shows approved report with created inspection link', async () => {
    const user = userEvent.setup();
    preventiveExecutionCandidateApi.getReport.mockResolvedValue(approvedReport);
    preventiveExecutionCandidateApi.get.mockResolvedValue({
      ...candidate,
      candidateStatus: 'APPROVED',
      createdInspectionId: 900,
    });

    render(
      <MemoryRouter>
        <PreventiveExecutionCandidatesPage />
      </MemoryRouter>
    );

    await screen.findByText('PUMP_MONTHLY');
    await user.click(screen.getByRole('button', { name: 'View' }));
    await user.click(screen.getByRole('tab', { name: 'Execution Report' }));

    const reportDetail = screen.getByText('Report Status').closest('dl');
    expect(within(reportDetail).getByText('Inspection created')).toBeInTheDocument();
    expect(
      within(reportDetail).getByRole('link', { name: /Inspection #900/i })
    ).toBeInTheDocument();
  });

  it('shows rejected report with decision reason', async () => {
    const user = userEvent.setup();
    preventiveExecutionCandidateApi.getReport.mockResolvedValue(rejectedReport);

    render(
      <MemoryRouter>
        <PreventiveExecutionCandidatesPage />
      </MemoryRouter>
    );

    await screen.findByText('PUMP_MONTHLY');
    await user.click(screen.getByRole('button', { name: 'View' }));
    await user.click(screen.getByRole('tab', { name: 'Execution Report' }));

    const reportDetail = screen.getByText('Report Status').closest('dl');
    expect(within(reportDetail).getByText('Rejected')).toBeInTheDocument();
    expect(within(reportDetail).getByText('Already inspected')).toBeInTheDocument();
  });

  it('shows dismissed report with decision comment', async () => {
    const user = userEvent.setup();
    preventiveExecutionCandidateApi.getReport.mockResolvedValue(dismissedReport);

    render(
      <MemoryRouter>
        <PreventiveExecutionCandidatesPage />
      </MemoryRouter>
    );

    await screen.findByText('PUMP_MONTHLY');
    await user.click(screen.getByRole('button', { name: 'View' }));
    await user.click(screen.getByRole('tab', { name: 'Execution Report' }));

    const reportDetail = screen.getByText('Report Status').closest('dl');
    expect(within(reportDetail).getByText('Dismissed')).toBeInTheDocument();
    expect(within(reportDetail).getByText('Not relevant')).toBeInTheDocument();
  });

  it('displays nextEligibleAt when present', async () => {
    preventiveExecutionCandidateApi.list.mockResolvedValue(
      pageResponse([
        {
          ...candidate,
          nextEligibleAt: 1712678400000,
        },
      ])
    );

    render(
      <MemoryRouter>
        <PreventiveExecutionCandidatesPage />
      </MemoryRouter>
    );

    expect(await screen.findByText('PUMP_MONTHLY')).toBeInTheDocument();
    const cells = screen.getAllByRole('cell');
    const formatted = formatTimestamp(1712678400000);
    expect(cells.some((cell) => cell.textContent === formatted)).toBe(true);
  });

  it('approves candidate and shows inspection link', async () => {
    const user = userEvent.setup();
    preventiveExecutionCandidateApi.approve.mockResolvedValue({
      candidate: { ...candidate, candidateStatus: 'APPROVED', createdInspectionId: 900 },
      inspection: { id: 900 },
    });

    render(
      <MemoryRouter>
        <PreventiveExecutionCandidatesPage />
      </MemoryRouter>
    );

    await screen.findByText('PUMP_MONTHLY');
    await user.click(screen.getAllByRole('button', { name: 'Approve' })[0]);
    expect(await screen.findByRole('heading', { name: 'Approve Candidate' })).toBeInTheDocument();

    const submitButton = screen.getByRole('button', { name: 'Approve and Create Inspection' });
    expect(submitButton).toBeDisabled();

    await user.selectOptions(screen.getByLabelText('Assignee'), '60');
    await user.type(screen.getByLabelText('Planned Date'), '2026-06-27');
    await user.type(screen.getByLabelText('Notes'), 'Monthly preventive check');
    expect(submitButton).not.toBeDisabled();

    await user.click(submitButton);

    await waitFor(() => {
      expect(preventiveExecutionCandidateApi.approve).toHaveBeenCalledWith(500, {
        assigneeId: 60,
        plannedAt: new Date('2026-06-27T00:00:00').getTime(),
        notes: 'Monthly preventive check',
      });
    });
    expect(
      await screen.findByText(/Candidate approved and inspection created/i)
    ).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Execute' })).not.toBeInTheDocument();
  });

  it('does not submit approval without assignee', async () => {
    const user = userEvent.setup();

    render(
      <MemoryRouter>
        <PreventiveExecutionCandidatesPage />
      </MemoryRouter>
    );

    await screen.findByText('PUMP_MONTHLY');
    await user.click(screen.getAllByRole('button', { name: 'Approve' })[0]);
    await screen.findByRole('heading', { name: 'Approve Candidate' });

    const submitButton = screen.getByRole('button', { name: 'Approve and Create Inspection' });
    expect(submitButton).toBeDisabled();
    expect(preventiveExecutionCandidateApi.approve).not.toHaveBeenCalled();
  });

  it('sends numeric assigneeId from worker id field', async () => {
    const user = userEvent.setup();
    preventiveExecutionCandidateApi.approve.mockResolvedValue({
      candidate: { ...candidate, candidateStatus: 'APPROVED', createdInspectionId: 900 },
      inspection: { id: 900 },
    });

    render(
      <MemoryRouter>
        <PreventiveExecutionCandidatesPage />
      </MemoryRouter>
    );

    await screen.findByText('PUMP_MONTHLY');
    await user.click(screen.getAllByRole('button', { name: 'Approve' })[0]);
    await user.selectOptions(screen.getByLabelText('Assignee'), '60');
    await user.click(screen.getByRole('button', { name: 'Approve and Create Inspection' }));

    await waitFor(() => {
      expect(preventiveExecutionCandidateApi.approve).toHaveBeenCalledWith(
        500,
        expect.objectContaining({
          assigneeId: 60,
        })
      );
    });

    const payload = preventiveExecutionCandidateApi.approve.mock.calls[0][1];
    expect(payload.assigneeId).not.toBeNull();
    expect(Number.isNaN(payload.assigneeId)).toBe(false);
    expect(typeof payload.assigneeId).toBe('number');
  });

  it('rejects candidate', async () => {
    const user = userEvent.setup();
    preventiveExecutionCandidateApi.reject.mockResolvedValue({
      ...candidate,
      candidateStatus: 'REJECTED',
      rejectionReason: 'Already inspected',
    });

    render(
      <MemoryRouter>
        <PreventiveExecutionCandidatesPage />
      </MemoryRouter>
    );

    await screen.findByText('PUMP_MONTHLY');
    await user.click(screen.getByRole('button', { name: 'Reject' }));
    await user.type(screen.getByLabelText('Reason'), 'Already inspected');
    await user.click(screen.getByRole('button', { name: 'Reject Candidate' }));

    await waitFor(() => {
      expect(preventiveExecutionCandidateApi.reject).toHaveBeenCalledWith(500, {
        reason: 'Already inspected',
      });
    });
  });
});
