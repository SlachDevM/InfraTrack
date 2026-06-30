import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, waitFor, cleanup, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import PreventiveSchedulerPage from '../../pages/PreventiveSchedulerPage';
import preventiveSchedulerApi from '../../services/preventiveSchedulerApi';
import { USER_ROLES } from '../../constants/userRoles';

const mockNavigate = vi.fn();

const { mockAuth, mockLogout } = vi.hoisted(() => ({
  mockAuth: { token: 'test-token', user: { userId: 1, role: 'ADMINISTRATOR' } },
  mockLogout: vi.fn(),
}));

vi.mock('../../services/preventiveSchedulerApi', () => ({
  default: {
    getStatus: vi.fn(),
    run: vi.fn(),
    listRuns: vi.fn(),
    getRun: vi.fn(),
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

const run = {
  id: 100,
  startedAt: 1710000000000,
  finishedAt: 1710000001000,
  durationMs: 1000,
  status: 'SUCCESS',
  triggeredBy: 'MANUAL',
  triggeredByUserId: 1,
  plansEvaluatedCount: 3,
  candidatesCreatedCount: 1,
  candidatesSkippedDuplicateCount: 1,
  plansNotEligibleCount: 1,
  errorMessage: null,
  createdAt: 1710000000000,
};

describe('PreventiveSchedulerPage', () => {
  afterEach(cleanup);

  beforeEach(() => {
    vi.clearAllMocks();
    mockAuth.user.role = USER_ROLES.ADMINISTRATOR;
    preventiveSchedulerApi.getStatus.mockResolvedValue({ enabled: false });
    preventiveSchedulerApi.listRuns.mockResolvedValue(pageResponse([run]));
    preventiveSchedulerApi.getRun.mockResolvedValue(run);
  });

  it('renders scheduler status and run history for administrator', async () => {
    const { container } = render(
      <MemoryRouter>
        <PreventiveSchedulerPage />
      </MemoryRouter>
    );

    expect(await screen.findByText('Preventive Scheduler')).toBeInTheDocument();
    expect(screen.getByText('Disabled')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Run Scheduler' })).toBeInTheDocument();
    expect(screen.getByText('Success')).toBeInTheDocument();
    expect(container.querySelector('.reference-header')).toBeInTheDocument();
    expect(container.querySelector('.reference-content')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: '← Back' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Notifications' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Logout' })).toBeInTheDocument();
  });

  it('shows Page 1 of 1 when run history is empty', async () => {
    preventiveSchedulerApi.listRuns.mockResolvedValue(pageResponse([], 0, 0));

    render(
      <MemoryRouter>
        <PreventiveSchedulerPage />
      </MemoryRouter>
    );

    expect(await screen.findByText('No scheduler runs found.')).toBeInTheDocument();
    expect(screen.getByText('Page 1 of 1')).toBeInTheDocument();
    expect(screen.queryByText(/NaN/)).not.toBeInTheDocument();
  });

  it('shows Run Scheduler for manager', async () => {
    mockAuth.user.role = USER_ROLES.MANAGER;

    render(
      <MemoryRouter>
        <PreventiveSchedulerPage />
      </MemoryRouter>
    );

    expect(await screen.findByText('Preventive Scheduler')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Run Scheduler' })).toBeInTheDocument();
  });

  it('hides Run Scheduler for operational coordinator', async () => {
    mockAuth.user.role = USER_ROLES.OPERATIONAL_COORDINATOR;

    render(
      <MemoryRouter>
        <PreventiveSchedulerPage />
      </MemoryRouter>
    );

    expect(await screen.findByText('Preventive Scheduler')).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Run Scheduler' })).not.toBeInTheDocument();
  });

  it('redirects field employee away from page', async () => {
    mockAuth.user.role = USER_ROLES.FIELD_EMPLOYEE;

    render(
      <MemoryRouter>
        <PreventiveSchedulerPage />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith('/');
    });
  });

  it('runs scheduler manually and shows result counts', async () => {
    const user = userEvent.setup();
    preventiveSchedulerApi.run.mockResolvedValue({
      runId: 101,
      status: 'SUCCESS',
      plansEvaluatedCount: 2,
      candidatesCreatedCount: 1,
      candidatesSkippedDuplicateCount: 1,
      plansNotEligibleCount: 0,
      durationMs: 500,
    });

    render(
      <MemoryRouter>
        <PreventiveSchedulerPage />
      </MemoryRouter>
    );

    await screen.findByText('Preventive Scheduler');
    await user.click(screen.getByRole('button', { name: 'Run Scheduler' }));

    await waitFor(() => {
      expect(preventiveSchedulerApi.run).toHaveBeenCalled();
    });
    expect(await screen.findByText(/Scheduler run complete/i)).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Create Inspection' })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Execute' })).not.toBeInTheDocument();
  });

  it('shows run detail with counts', async () => {
    const user = userEvent.setup();

    render(
      <MemoryRouter>
        <PreventiveSchedulerPage />
      </MemoryRouter>
    );

    await screen.findByText('Preventive Scheduler');
    await user.click(screen.getByRole('button', { name: 'View' }));

    expect(await screen.findByText('Run Detail')).toBeInTheDocument();
    const detail = screen.getByText('Run Detail').closest('section');
    expect(within(detail).getByText('Manual')).toBeInTheDocument();
    expect(within(detail).getByText('Candidates Created')).toBeInTheDocument();
  });
});
