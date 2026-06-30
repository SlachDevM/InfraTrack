import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, waitFor, cleanup, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import DashboardPage from '../../pages/DashboardPage';
import operationsIntelligenceApi from '../../services/operationsIntelligenceApi';
import { USER_ROLES } from '../../constants/userRoles';

const mockNavigate = vi.fn();
const mockLogout = vi.fn();

const { mockAuth } = vi.hoisted(() => ({
  mockAuth: {
    token: 'test-token',
    user: { userId: 1, name: 'Alex Manager', email: 'manager@test.com', role: 'MANAGER' },
  },
}));

vi.mock('../../services/operationsIntelligenceApi', () => ({
  default: {
    getKpis: vi.fn(),
    getTrends: vi.fn(),
    getRecentActivity: vi.fn(),
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

const sampleKpis = {
  assets: {
    totalAssets: 12,
    assetsWithoutCategory: 1,
    assetsWithoutDepartment: 0,
  },
  inspections: {
    assignedInspections: 5,
    completedInspections: 20,
    overdueInspections: 2,
  },
  issues: {
    openIssues: 3,
    resolvedIssues: 8,
    reworkIssues: 1,
    issuesBySeverity: { HIGH: 1, CRITICAL: 0, MEDIUM: 2, LOW: 0 },
    issuesByType: { NORMAL: 3, REWORK: 1 },
  },
  workOrders: {
    openWorkOrders: 4,
    inProgressWorkOrders: 2,
    completedWorkOrders: 10,
    overdueWorkOrders: 0,
  },
  preventive: {
    activePreventivePlans: 6,
    pausedPreventivePlans: 1,
    pendingExecutionCandidates: 2,
    approvedExecutionCandidates: 1,
    rejectedExecutionCandidates: 0,
    dismissedExecutionCandidates: 0,
    schedulerRunsToday: 1,
    eligiblePlansNow: 3,
  },
  decisionEngine: {
    ruleEvaluationReports: 7,
    suggestedActionsPending: 4,
    suggestedActionsAccepted: 2,
    suggestedActionsRejected: 1,
    suggestedActionsDismissed: 0,
    matchedRuleResults: 9,
  },
};

const zeroAlertKpis = {
  ...sampleKpis,
  inspections: { ...sampleKpis.inspections, overdueInspections: 0 },
  issues: { ...sampleKpis.issues, openIssues: 0, issuesBySeverity: {} },
  preventive: { ...sampleKpis.preventive, pendingExecutionCandidates: 0 },
  decisionEngine: { ...sampleKpis.decisionEngine, suggestedActionsPending: 0 },
};

const sampleTrends = {
  from: 1717200000000,
  to: 1719792000000,
  bucket: 'DAY',
  scope: { type: 'DEPARTMENT', departmentId: 10 },
  series: {
    inspectionsCompleted: [
      { period: '2026-06-01', count: 4 },
      { period: '2026-06-02', count: 0 },
    ],
    issuesCreated: [
      { period: '2026-06-01', count: 2 },
      { period: '2026-06-02', count: 1 },
    ],
    workOrdersCompleted: [{ period: '2026-06-01', count: 1 }],
    preventiveCandidatesGenerated: [{ period: '2026-06-01', count: 3 }],
    suggestedActionsAccepted: [{ period: '2026-06-02', count: 1 }],
  },
};

const emptyTrends = {
  ...sampleTrends,
  series: {
    inspectionsCompleted: [{ period: '2026-06-01', count: 0 }],
    issuesCreated: [{ period: '2026-06-01', count: 0 }],
    workOrdersCompleted: [{ period: '2026-06-01', count: 0 }],
    preventiveCandidatesGenerated: [{ period: '2026-06-01', count: 0 }],
    suggestedActionsAccepted: [{ period: '2026-06-01', count: 0 }],
  },
};

const sampleRecentActivity = {
  items: [
    {
      type: 'INSPECTION_COMPLETED',
      title: 'Inspection completed',
      description: 'Street Light 001',
      assetId: 12,
      assetName: 'Street Light 001',
      occurredAt: 1719792000000,
      route: '/inspections',
    },
    {
      type: 'ISSUE_CREATED',
      title: 'Issue created',
      description: 'Park BBQ',
      assetId: 5,
      assetName: 'Park BBQ',
      occurredAt: 1719705600000,
      route: '/issues',
    },
  ],
};

describe('DashboardPage', () => {
  afterEach(cleanup);

  beforeEach(() => {
    vi.clearAllMocks();
    mockAuth.user.role = USER_ROLES.MANAGER;
    operationsIntelligenceApi.getKpis.mockResolvedValue(sampleKpis);
    operationsIntelligenceApi.getTrends.mockResolvedValue(sampleTrends);
    operationsIntelligenceApi.getRecentActivity.mockResolvedValue(sampleRecentActivity);
  });

  it('loads KPIs from API and displays cards', async () => {
    render(
      <MemoryRouter>
        <DashboardPage />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(operationsIntelligenceApi.getKpis).toHaveBeenCalled();
      expect(operationsIntelligenceApi.getTrends).toHaveBeenCalledWith({ bucket: 'DAY' });
      expect(operationsIntelligenceApi.getRecentActivity).toHaveBeenCalledWith({ limit: 20 });
    });

    expect(await screen.findByText('Good morning, Alex Manager')).toBeInTheDocument();
    expect(screen.getByText('Operational overview')).toBeInTheDocument();
    expect(screen.getAllByText('12').length).toBeGreaterThan(0);
    expect(screen.getByText('Open Issues')).toBeInTheDocument();
    expect(screen.getByText('Pending Preventive Candidates')).toBeInTheDocument();

    const inspectionsCard = screen.getByRole('heading', { name: 'Inspections' }).closest('article');
    expect(within(inspectionsCard).getByText('Overdue')).toBeInTheDocument();
    expect(within(inspectionsCard).getByText('2')).toBeInTheDocument();
  });

  it('displays attention alerts when KPI values are non-zero', async () => {
    render(
      <MemoryRouter>
        <DashboardPage />
      </MemoryRouter>
    );

    expect(await screen.findByText('Attention required')).toBeInTheDocument();
    expect(screen.getByText('Overdue inspections require attention.')).toBeInTheDocument();
    expect(screen.getByText('Open issues are waiting for review.')).toBeInTheDocument();
    expect(screen.getByText('Preventive candidates are waiting for decision.')).toBeInTheDocument();
    expect(screen.getByText('Suggested actions are waiting for manager review.')).toBeInTheDocument();
  });

  it('hides attention alerts when all relevant values are zero', async () => {
    operationsIntelligenceApi.getKpis.mockResolvedValue(zeroAlertKpis);

    render(
      <MemoryRouter>
        <DashboardPage />
      </MemoryRouter>
    );

    await screen.findByText('Operational KPIs');
    expect(screen.queryByText('Attention required')).not.toBeInTheDocument();
  });

  it('displays quick navigation links according to role', async () => {
    render(
      <MemoryRouter>
        <DashboardPage />
      </MemoryRouter>
    );

    const quickNavHeading = await screen.findByText('Quick navigation');
    const quickNavSection = quickNavHeading.closest('section');
    expect(within(quickNavSection).getByRole('button', { name: 'Assets' })).toBeInTheDocument();
    expect(within(quickNavSection).getByRole('button', { name: 'Preventive Candidates' })).toBeInTheDocument();
    expect(within(quickNavSection).getByRole('button', { name: 'Scheduler' })).toBeInTheDocument();
  });

  it('renders recent activity widget with items', async () => {
    render(
      <MemoryRouter>
        <DashboardPage />
      </MemoryRouter>
    );

    expect(await screen.findByText('Recent activity')).toBeInTheDocument();
    expect(screen.getByText('Inspection completed')).toBeInTheDocument();
    expect(screen.getByText('Street Light 001')).toBeInTheDocument();
    expect(screen.getByText('Issue created')).toBeInTheDocument();
  });

  it('shows recent activity loading state', () => {
    operationsIntelligenceApi.getRecentActivity.mockImplementation(() => new Promise(() => {}));

    render(
      <MemoryRouter>
        <DashboardPage />
      </MemoryRouter>
    );

    expect(screen.getByText('Loading recent activity...')).toBeInTheDocument();
  });

  it('shows recent activity error state', async () => {
    operationsIntelligenceApi.getRecentActivity.mockRejectedValue({
      status: 500,
      message: 'Activity service unavailable',
    });

    render(
      <MemoryRouter>
        <DashboardPage />
      </MemoryRouter>
    );

    expect(await screen.findByText('Activity service unavailable')).toBeInTheDocument();
  });

  it('shows recent activity empty state', async () => {
    operationsIntelligenceApi.getRecentActivity.mockResolvedValue({ items: [] });

    render(
      <MemoryRouter>
        <DashboardPage />
      </MemoryRouter>
    );

    expect(await screen.findByText('No recent operational activity.')).toBeInTheDocument();
  });

  it('navigates when recent activity item is clicked', async () => {
    const user = userEvent.setup();

    render(
      <MemoryRouter>
        <DashboardPage />
      </MemoryRouter>
    );

    const activityButton = await screen.findByRole('button', { name: /Inspection completed/i });
    await user.click(activityButton);

    expect(mockNavigate).toHaveBeenCalledWith('/inspections');
  });

  it('shows trends loading state', () => {
    operationsIntelligenceApi.getTrends.mockImplementation(() => new Promise(() => {}));

    render(
      <MemoryRouter>
        <DashboardPage />
      </MemoryRouter>
    );

    expect(screen.getByText('Loading operational trends...')).toBeInTheDocument();
  });

  it('shows trends error state', async () => {
    operationsIntelligenceApi.getTrends.mockRejectedValue({
      status: 500,
      message: 'Trend service unavailable',
    });

    render(
      <MemoryRouter>
        <DashboardPage />
      </MemoryRouter>
    );

    expect(await screen.findByText('Trend service unavailable')).toBeInTheDocument();
  });

  it('renders trend section with at least one series', async () => {
    render(
      <MemoryRouter>
        <DashboardPage />
      </MemoryRouter>
    );

    expect(await screen.findByText('Operational trends (last 30 days)')).toBeInTheDocument();
    expect(screen.getByText('Inspections completed')).toBeInTheDocument();
    expect(screen.getByText('Issues created')).toBeInTheDocument();
    expect(screen.getAllByText('Total:').length).toBeGreaterThan(0);
  });

  it('shows empty state for trend series with no activity', async () => {
    operationsIntelligenceApi.getTrends.mockResolvedValue(emptyTrends);

    render(
      <MemoryRouter>
        <DashboardPage />
      </MemoryRouter>
    );

    expect(await screen.findAllByText('No activity in this period.')).not.toHaveLength(0);
  });

  it('still renders KPI cards when trends load', async () => {
    render(
      <MemoryRouter>
        <DashboardPage />
      </MemoryRouter>
    );

    await screen.findByText('Operational KPIs');
    expect(screen.getByText('Open Issues')).toBeInTheDocument();
  });

  it('shows loading state', () => {
    operationsIntelligenceApi.getKpis.mockImplementation(() => new Promise(() => {}));

    render(
      <MemoryRouter>
        <DashboardPage />
      </MemoryRouter>
    );

    expect(screen.getByText('Loading operational KPIs...')).toBeInTheDocument();
  });

  it('shows error state', async () => {
    operationsIntelligenceApi.getKpis.mockRejectedValue({
      status: 403,
      message: 'Forbidden',
    });

    render(
      <MemoryRouter>
        <DashboardPage />
      </MemoryRouter>
    );

    expect(await screen.findByText('Forbidden')).toBeInTheDocument();
  });

  it('does not render workflow mutation action buttons', async () => {
    render(
      <MemoryRouter>
        <DashboardPage />
      </MemoryRouter>
    );

    await screen.findByText('Operational KPIs');
    expect(screen.queryByRole('button', { name: /Approve/i })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: /Reject/i })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: /Generate Candidates/i })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: /Run Scheduler/i })).not.toBeInTheDocument();
  });

  it('redirects field employees away from dashboard', async () => {
    mockAuth.user.role = USER_ROLES.FIELD_EMPLOYEE;

    render(
      <MemoryRouter>
        <DashboardPage />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith('/');
    });
    expect(operationsIntelligenceApi.getKpis).not.toHaveBeenCalled();
  });
});
