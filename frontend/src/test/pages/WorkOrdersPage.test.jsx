import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, waitFor, cleanup } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import WorkOrdersPage from '../../pages/WorkOrdersPage';
import workOrderApi from '../../services/workOrderApi';
import operationalDecisionApi from '../../services/operationalDecisionApi';
import maintenanceActivityApi from '../../services/maintenanceActivityApi';
import { DEFAULT_PAGE, MAX_PAGE_SIZE } from '../../utils/pagination';

const mockNavigate = vi.fn();

const { mockAuth, mockLogout } = vi.hoisted(() => ({
  mockAuth: { token: 'test-token', user: { userId: 40, role: 'OPERATIONAL_COORDINATOR' } },
  mockLogout: vi.fn(),
}));

vi.mock('../../services/workOrderApi', () => ({
  default: {
    list: vi.fn(),
    listEligibleForAssignment: vi.fn(),
    create: vi.fn(),
    assign: vi.fn(),
    listEligibleWorkers: vi.fn(),
  },
}));

vi.mock('../../services/operationalDecisionApi', () => ({
  default: {
    listEligibleForWorkOrderCreation: vi.fn(),
  },
}));

vi.mock('../../services/maintenanceActivityApi', () => ({
  default: {
    list: vi.fn(),
    listEligibleForCompletionReview: vi.fn(),
    recordCompletionReview: vi.fn(),
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

const eligibleDecision = {
  id: 1,
  assetName: 'Central Playground',
  outcome: 'INTERNAL_MAINTENANCE',
  rationale: 'Replace damaged chain',
};

const assignableWorkOrder = {
  id: 100,
  assetName: 'Central Playground',
  assetDepartmentId: 1,
  workType: 'INTERNAL_MAINTENANCE',
  status: 'CREATED',
  description: 'Replace chain',
  operationalDecisionId: 1,
};

describe('WorkOrdersPage', () => {
  afterEach(cleanup);

  beforeEach(() => {
    vi.clearAllMocks();
    mockAuth.user = { userId: 40, role: 'OPERATIONAL_COORDINATOR' };
    workOrderApi.list.mockResolvedValue(pageResponse([]));
    workOrderApi.listEligibleForAssignment.mockResolvedValue(pageResponse([]));
    operationalDecisionApi.listEligibleForWorkOrderCreation.mockResolvedValue(pageResponse([]));
    maintenanceActivityApi.list.mockResolvedValue(pageResponse([]));
    maintenanceActivityApi.listEligibleForCompletionReview.mockResolvedValue(pageResponse([]));
    workOrderApi.listEligibleWorkers.mockResolvedValue([]);
  });

  it('shows eligible operational decision in create selector from backend response', async () => {
    operationalDecisionApi.listEligibleForWorkOrderCreation.mockResolvedValue(
      pageResponse([eligibleDecision])
    );

    render(
      <MemoryRouter>
        <WorkOrdersPage />
      </MemoryRouter>
    );

    expect(
      await screen.findByRole('option', {
        name: /#1 — Central Playground \(Internal Maintenance\)/i,
      })
    ).toBeInTheDocument();
    expect(operationalDecisionApi.listEligibleForWorkOrderCreation).toHaveBeenCalledWith(
      DEFAULT_PAGE,
      MAX_PAGE_SIZE
    );
  });

  it('shows only assignable work orders from backend in assignment selector', async () => {
    workOrderApi.listEligibleForAssignment.mockResolvedValue(pageResponse([assignableWorkOrder]));

    render(
      <MemoryRouter>
        <WorkOrdersPage />
      </MemoryRouter>
    );

    expect(
      await screen.findByRole('option', {
        name: /#100 — Central Playground/i,
      })
    ).toBeInTheDocument();
    expect(workOrderApi.listEligibleForAssignment).toHaveBeenCalledWith(
      DEFAULT_PAGE,
      MAX_PAGE_SIZE
    );
    expect(
      screen.queryByRole('option', { name: /Other Department Asset/i })
    ).not.toBeInTheDocument();
  });

  it('loads eligible assignees when work order is selected', async () => {
    const user = userEvent.setup();
    workOrderApi.listEligibleForAssignment.mockResolvedValue(pageResponse([assignableWorkOrder]));
    workOrderApi.listEligibleWorkers.mockResolvedValue([
      { id: 20, name: 'Alex Field', role: 'FIELD_EMPLOYEE', status: 'ACTIVE', departmentId: 1 },
    ]);

    render(
      <MemoryRouter>
        <WorkOrdersPage />
      </MemoryRouter>
    );

    await user.selectOptions(await screen.findByLabelText('Work Order'), '100');

    await waitFor(() => {
      expect(workOrderApi.listEligibleWorkers).toHaveBeenCalledWith(1, 'FIELD_EMPLOYEE');
      expect(screen.getByRole('option', { name: /Alex Field/i })).toBeInTheDocument();
    });
  });

  it('does not show disabled or incompatible assignees from backend response', async () => {
    const user = userEvent.setup();
    workOrderApi.listEligibleForAssignment.mockResolvedValue(pageResponse([assignableWorkOrder]));
    workOrderApi.listEligibleWorkers.mockResolvedValue([
      { id: 20, name: 'Alex Field', role: 'FIELD_EMPLOYEE', status: 'ACTIVE', departmentId: 1 },
    ]);

    render(
      <MemoryRouter>
        <WorkOrdersPage />
      </MemoryRouter>
    );

    await user.selectOptions(await screen.findByLabelText('Work Order'), '100');

    await waitFor(() => {
      expect(screen.getByRole('option', { name: /Alex Field/i })).toBeInTheDocument();
    });

    expect(screen.queryByRole('option', { name: /Disabled/i })).not.toBeInTheDocument();
    expect(screen.queryByRole('option', { name: /MANAGER/i })).not.toBeInTheDocument();
    expect(screen.queryByRole('option', { name: /CONTRACTOR/i })).not.toBeInTheDocument();
  });

  it('displays forbidden message when work order creation is rejected', async () => {
    const user = userEvent.setup();
    operationalDecisionApi.listEligibleForWorkOrderCreation.mockResolvedValue(
      pageResponse([eligibleDecision])
    );
    workOrderApi.create.mockRejectedValue({ status: 403 });

    render(
      <MemoryRouter>
        <WorkOrdersPage />
      </MemoryRouter>
    );

    await user.selectOptions(await screen.findByLabelText('Operational Decision'), '1');
    await user.type(screen.getByLabelText('Description'), 'Replace chain');
    await user.click(screen.getByRole('button', { name: 'Create Work Order' }));

    await waitFor(() => {
      expect(
        screen.getByText('You do not have permission to create work orders.')
      ).toBeInTheDocument();
    });
  });

  it('displays forbidden message when assignment is rejected', async () => {
    const user = userEvent.setup();
    workOrderApi.listEligibleForAssignment.mockResolvedValue(pageResponse([assignableWorkOrder]));
    workOrderApi.listEligibleWorkers.mockResolvedValue([
      { id: 20, name: 'Alex Field', role: 'FIELD_EMPLOYEE', status: 'ACTIVE', departmentId: 1 },
    ]);
    workOrderApi.assign.mockRejectedValue({ status: 403 });

    render(
      <MemoryRouter>
        <WorkOrdersPage />
      </MemoryRouter>
    );

    await user.selectOptions(await screen.findByLabelText('Work Order'), '100');
    await user.selectOptions(await screen.findByLabelText('Assign To'), '20');
    await user.click(screen.getByRole('button', { name: 'Assign Work Order' }));

    await waitFor(() => {
      expect(
        screen.getByText('You do not have permission to assign work orders.')
      ).toBeInTheDocument();
    });
  });
});

describe('WorkOrdersPage completion review', () => {
  afterEach(cleanup);

  beforeEach(() => {
    vi.clearAllMocks();
    mockAuth.user = { userId: 30, role: 'MANAGER' };
    workOrderApi.list.mockResolvedValue(pageResponse([]));
    maintenanceActivityApi.list.mockResolvedValue(pageResponse([]));
    maintenanceActivityApi.listEligibleForCompletionReview.mockResolvedValue(pageResponse([]));
  });

  it('shows only eligible maintenance activities from backend in review selector', async () => {
    maintenanceActivityApi.listEligibleForCompletionReview.mockResolvedValue(
      pageResponse([
        {
          id: 500,
          workOrderId: 100,
          assetName: 'Central Playground',
          workOrderStatus: 'COMPLETED',
          completionReviewDecision: null,
        },
      ])
    );

    render(
      <MemoryRouter>
        <WorkOrdersPage />
      </MemoryRouter>
    );

    expect(
      await screen.findByRole('option', {
        name: /#500 — WO #100 — Central Playground/i,
      })
    ).toBeInTheDocument();
    expect(maintenanceActivityApi.listEligibleForCompletionReview).toHaveBeenCalledWith(
      DEFAULT_PAGE,
      MAX_PAGE_SIZE
    );
    expect(
      screen.queryByRole('option', { name: /Other Department Asset/i })
    ).not.toBeInTheDocument();
  });

  it('hides already reviewed maintenance activities from backend response', async () => {
    maintenanceActivityApi.listEligibleForCompletionReview.mockResolvedValue(
      pageResponse([
        {
          id: 500,
          workOrderId: 100,
          assetName: 'Central Playground',
          workOrderStatus: 'COMPLETED',
          completionReviewDecision: null,
        },
      ])
    );

    render(
      <MemoryRouter>
        <WorkOrdersPage />
      </MemoryRouter>
    );

    await screen.findByRole('option', { name: /#500 — WO #100 — Central Playground/i });
    expect(
      screen.queryByRole('option', { name: /Already Reviewed Asset/i })
    ).not.toBeInTheDocument();
  });

  it('displays forbidden message when completion review is rejected', async () => {
    const user = userEvent.setup();
    maintenanceActivityApi.listEligibleForCompletionReview.mockResolvedValue(
      pageResponse([
        {
          id: 500,
          workOrderId: 100,
          assetName: 'Central Playground',
          workOrderStatus: 'COMPLETED',
          completionReviewDecision: null,
        },
      ])
    );
    maintenanceActivityApi.recordCompletionReview.mockRejectedValue({ status: 403 });

    render(
      <MemoryRouter>
        <WorkOrdersPage />
      </MemoryRouter>
    );

    await user.selectOptions(await screen.findByLabelText('Maintenance Activity'), '500');
    await user.type(screen.getByLabelText('Review Notes'), 'Work completed to standard');
    await user.click(screen.getByRole('button', { name: 'Record Completion Review' }));

    await waitFor(() => {
      expect(
        screen.getByText(
          'You do not have permission to record completion reviews for this maintenance activity.'
        )
      ).toBeInTheDocument();
    });
  });

  it('displays rework success message when REWORK_REQUIRED is recorded', async () => {
    const user = userEvent.setup();
    maintenanceActivityApi.listEligibleForCompletionReview.mockResolvedValue(
      pageResponse([
        {
          id: 500,
          workOrderId: 100,
          assetName: 'Central Playground',
          workOrderStatus: 'COMPLETED',
          completionReviewDecision: null,
        },
      ])
    );
    maintenanceActivityApi.recordCompletionReview.mockResolvedValue({
      decision: 'REWORK_REQUIRED',
      reworkIssueId: 8001,
    });

    render(
      <MemoryRouter>
        <WorkOrdersPage />
      </MemoryRouter>
    );

    await user.selectOptions(await screen.findByLabelText('Maintenance Activity'), '500');
    await user.selectOptions(screen.getByLabelText('Review Decision'), 'REWORK_REQUIRED');
    await user.selectOptions(screen.getByLabelText('Rework Severity'), 'HIGH');
    await user.type(screen.getByLabelText('Review Notes'), 'Chain still loose, rework required');
    await user.type(screen.getByLabelText('Root Cause'), 'Missing lubrication');
    await user.click(screen.getByRole('button', { name: 'Record Completion Review' }));

    await waitFor(() => {
      expect(maintenanceActivityApi.recordCompletionReview).toHaveBeenCalledWith(500, {
        decision: 'REWORK_REQUIRED',
        reviewNotes: 'Chain still loose, rework required',
        reviewedAt: expect.any(String),
        reworkSeverity: 'HIGH',
        rootCause: 'Missing lubrication',
      });
      expect(
        screen.getByText(
          'Completion Review recorded. A rework Issue has been created for managerial decision.'
        )
      ).toBeInTheDocument();
      expect(screen.getByRole('link', { name: 'Go to Operational Decisions' })).toHaveAttribute(
        'href',
        '/operational-decisions'
      );
    });
  });

  it('shows rework fields when REWORK_REQUIRED is selected and hides them for APPROVED', async () => {
    const user = userEvent.setup();
    maintenanceActivityApi.listEligibleForCompletionReview.mockResolvedValue(
      pageResponse([
        {
          id: 500,
          workOrderId: 100,
          assetName: 'Central Playground',
          workOrderStatus: 'COMPLETED',
          completionReviewDecision: null,
        },
      ])
    );

    render(
      <MemoryRouter>
        <WorkOrdersPage />
      </MemoryRouter>
    );

    await screen.findByLabelText('Review Decision');
    expect(screen.queryByLabelText('Rework Severity')).not.toBeInTheDocument();
    expect(screen.queryByLabelText('Root Cause')).not.toBeInTheDocument();

    await user.selectOptions(screen.getByLabelText('Review Decision'), 'REWORK_REQUIRED');

    expect(screen.getByLabelText('Rework Severity')).toBeInTheDocument();
    expect(screen.getByLabelText('Root Cause')).toBeInTheDocument();
    expect(screen.getByLabelText('Corrective Action')).toBeInTheDocument();
    expect(screen.getByLabelText('Preventive Action')).toBeInTheDocument();

    await user.selectOptions(screen.getByLabelText('Review Decision'), 'APPROVED');

    expect(screen.queryByLabelText('Rework Severity')).not.toBeInTheDocument();
    expect(screen.queryByLabelText('Root Cause')).not.toBeInTheDocument();
  });

  it('displays standard success message when APPROVED is recorded', async () => {
    const user = userEvent.setup();
    maintenanceActivityApi.listEligibleForCompletionReview.mockResolvedValue(
      pageResponse([
        {
          id: 500,
          workOrderId: 100,
          assetName: 'Central Playground',
          workOrderStatus: 'COMPLETED',
          completionReviewDecision: null,
        },
      ])
    );
    maintenanceActivityApi.recordCompletionReview.mockResolvedValue({
      decision: 'APPROVED',
      reworkIssueId: null,
    });

    render(
      <MemoryRouter>
        <WorkOrdersPage />
      </MemoryRouter>
    );

    await user.selectOptions(await screen.findByLabelText('Maintenance Activity'), '500');
    await user.type(screen.getByLabelText('Review Notes'), 'Work completed to standard');
    await user.click(screen.getByRole('button', { name: 'Record Completion Review' }));

    await waitFor(() => {
      expect(screen.getByText('Completion review recorded successfully.')).toBeInTheDocument();
      expect(
        screen.queryByRole('link', { name: 'Go to Operational Decisions' })
      ).not.toBeInTheDocument();
    });
  });
});
