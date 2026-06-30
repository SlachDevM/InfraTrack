import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, waitFor, cleanup } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import PlatformShell from '../../pages/PlatformShell';
import DashboardPage from '../../pages/DashboardPage';
import operationsIntelligenceApi from '../../services/operationsIntelligenceApi';
import { USER_ROLES } from '../../constants/userRoles';

const mockNavigate = vi.fn();
const mockLogout = vi.fn();

const { mockAuth } = vi.hoisted(() => ({
  mockAuth: {
    token: 'test-token',
    user: { userId: 10, email: 'user@test.com', role: 'FIELD_EMPLOYEE' },
  },
}));

vi.mock('../../services/operationsIntelligenceApi', () => ({
  default: {
    getKpis: vi.fn(),
  },
}));

vi.mock('../../services/apiClient', () => ({
  default: {
    setToken: vi.fn(),
  },
}));

vi.mock('../../context/AuthContext', () => ({
  useAuth: () => ({
    auth: mockAuth,
    logout: mockLogout,
  }),
}));

vi.mock('../../components/NotificationButton', () => ({
  default: () => <button type="button">Notifications</button>,
}));

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

describe('PlatformShell navigation', () => {
  afterEach(cleanup);

  beforeEach(() => {
    vi.clearAllMocks();
    operationsIntelligenceApi.getKpis.mockResolvedValue({
      assets: { totalAssets: 0 },
      inspections: { overdueInspections: 0 },
      issues: { openIssues: 0, issuesBySeverity: {} },
      preventive: { pendingExecutionCandidates: 0 },
      decisionEngine: { suggestedActionsPending: 0 },
    });
  });

  it('shows only operational nav items for field employees', () => {
    mockAuth.user.role = USER_ROLES.FIELD_EMPLOYEE;

    render(
      <MemoryRouter>
        <PlatformShell />
      </MemoryRouter>
    );

    expect(screen.getByRole('button', { name: 'Documents' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Inspections' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Work Orders' })).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Dashboard' })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'More' })).not.toBeInTheDocument();
    expect(screen.getByText('Assigned Inspections')).toBeInTheDocument();
  });

  it('redirects managers to dashboard', async () => {
    mockAuth.user.role = USER_ROLES.MANAGER;

    render(
      <MemoryRouter>
        <PlatformShell />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith('/dashboard', { replace: true });
    });
  });

  it('redirects operational coordinators to dashboard', async () => {
    mockAuth.user.role = USER_ROLES.OPERATIONAL_COORDINATOR;

    render(
      <MemoryRouter>
        <PlatformShell />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith('/dashboard', { replace: true });
    });
  });
});

describe('Dashboard navigation', () => {
  afterEach(cleanup);

  beforeEach(() => {
    vi.clearAllMocks();
    mockAuth.user.role = USER_ROLES.MANAGER;
    operationsIntelligenceApi.getKpis.mockResolvedValue({
      assets: { totalAssets: 1, assetsWithoutCategory: 0, assetsWithoutDepartment: 0 },
      inspections: { assignedInspections: 0, completedInspections: 0, overdueInspections: 0 },
      issues: { openIssues: 0, resolvedIssues: 0, reworkIssues: 0, issuesBySeverity: {} },
      workOrders: { openWorkOrders: 0, inProgressWorkOrders: 0, completedWorkOrders: 0, overdueWorkOrders: 0 },
      preventive: {
        activePreventivePlans: 0,
        pendingExecutionCandidates: 0,
        approvedExecutionCandidates: 0,
        schedulerRunsToday: 0,
        eligiblePlansNow: 0,
      },
      decisionEngine: {
        ruleEvaluationReports: 0,
        suggestedActionsPending: 0,
        suggestedActionsAccepted: 0,
        suggestedActionsRejected: 0,
        suggestedActionsDismissed: 0,
      },
    });
  });

  it('shows Dashboard link for manager and keeps More menu working', async () => {
    const user = userEvent.setup();

    render(
      <MemoryRouter>
        <DashboardPage />
      </MemoryRouter>
    );

    expect(await screen.findByRole('button', { name: 'Dashboard' })).toBeInTheDocument();
    expect(screen.getAllByRole('button', { name: 'Assets' }).length).toBeGreaterThan(0);
    expect(screen.getByRole('button', { name: 'More' })).toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: 'More' }));
    expect(screen.getByRole('menuitem', { name: 'Departments' })).toBeInTheDocument();
  });

  it('shows Dashboard link for administrator', async () => {
    mockAuth.user.role = USER_ROLES.ADMINISTRATOR;

    render(
      <MemoryRouter>
        <DashboardPage />
      </MemoryRouter>
    );

    expect(await screen.findByRole('button', { name: 'Dashboard' })).toBeInTheDocument();
  });

  it('shows Dashboard link for operational coordinator', async () => {
    mockAuth.user.role = USER_ROLES.OPERATIONAL_COORDINATOR;

    render(
      <MemoryRouter>
        <DashboardPage />
      </MemoryRouter>
    );

    expect(await screen.findByRole('button', { name: 'Dashboard' })).toBeInTheDocument();
  });
});
