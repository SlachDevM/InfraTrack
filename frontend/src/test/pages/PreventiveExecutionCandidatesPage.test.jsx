import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, waitFor, cleanup } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import PreventiveExecutionCandidatesPage from '../../pages/PreventiveExecutionCandidatesPage';
import preventiveExecutionCandidateApi from '../../services/preventiveExecutionCandidateApi';
import preventiveMaintenancePlanApi from '../../services/preventiveMaintenancePlanApi';
import assetApi from '../../services/assetApi';
import inspectionApi from '../../services/inspectionApi';
import userApi from '../../services/userApi';
import { USER_ROLES } from '../../constants/userRoles';

const mockNavigate = vi.fn();

const { mockAuth, mockLogout } = vi.hoisted(() => ({
  mockAuth: { token: 'test-token', user: { userId: 1, role: 'ADMINISTRATOR' } },
  mockLogout: vi.fn(),
}));

vi.mock('../../services/preventiveExecutionCandidateApi', () => ({
  default: {
    list: vi.fn(),
    get: vi.fn(),
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
const workers = [{ userId: 60, name: 'Field Worker', role: 'FIELD_EMPLOYEE', status: 'ACTIVE', departmentId: 10 }];

describe('PreventiveExecutionCandidatesPage', () => {
  afterEach(cleanup);

  beforeEach(() => {
    vi.clearAllMocks();
    mockAuth.user.role = USER_ROLES.ADMINISTRATOR;
    preventiveExecutionCandidateApi.list.mockResolvedValue(pageResponse([candidate]));
    preventiveExecutionCandidateApi.get.mockResolvedValue(candidate);
    assetApi.list.mockResolvedValue(pageResponse(assets));
    preventiveMaintenancePlanApi.list.mockResolvedValue(pageResponse(plans));
    inspectionApi.listWorkers.mockResolvedValue(workers);
    userApi.getCurrentUser.mockResolvedValue({ departmentId: 10 });
  });

  it('renders candidate list for administrator', async () => {
    render(
      <MemoryRouter>
        <PreventiveExecutionCandidatesPage />
      </MemoryRouter>
    );

    expect(await screen.findByText('PUMP_MONTHLY')).toBeInTheDocument();
    expect(screen.getByText('Monthly Pump Inspection')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Generate Candidates' })).toBeInTheDocument();
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
    });
    expect(await screen.findByText('Candidate Detail')).toBeInTheDocument();
    expect(screen.getByText('One full month has elapsed.')).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Execute' })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Create Inspection' })).not.toBeInTheDocument();
  });

  it('displays nextEligibleAt when present', async () => {
    preventiveExecutionCandidateApi.list.mockResolvedValue(pageResponse([
      {
        ...candidate,
        nextEligibleAt: 1712678400000,
      },
    ]));

    render(
      <MemoryRouter>
        <PreventiveExecutionCandidatesPage />
      </MemoryRouter>
    );

    expect(await screen.findByText('PUMP_MONTHLY')).toBeInTheDocument();
    const cells = screen.getAllByRole('cell');
    const formatted = new Date(1712678400000).toLocaleString();
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

    await user.selectOptions(screen.getByLabelText('Assignee'), '60');
    await user.click(screen.getByRole('button', { name: 'Approve and Create Inspection' }));

    await waitFor(() => {
      expect(preventiveExecutionCandidateApi.approve).toHaveBeenCalledWith(500, expect.objectContaining({
        assigneeId: 60,
      }));
    });
    expect(await screen.findByText(/Candidate approved and inspection created/i)).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Execute' })).not.toBeInTheDocument();
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
