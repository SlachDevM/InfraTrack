import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, waitFor, cleanup } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import WorkOrdersPage from '../../pages/WorkOrdersPage';
import workOrderApi from '../../services/workOrderApi';
import operationalDecisionApi from '../../services/operationalDecisionApi';
import maintenanceActivityApi from '../../services/maintenanceActivityApi';
import { MAX_PAGE_SIZE } from '../../utils/pagination';

const mockNavigate = vi.fn();

const { mockAuth, mockLogout } = vi.hoisted(() => ({
  mockAuth: { token: 'test-token', user: { userId: 40, role: 'OPERATIONAL_COORDINATOR' } },
  mockLogout: vi.fn(),
}));

vi.mock('../../services/workOrderApi', () => ({
  default: {
    list: vi.fn(),
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

const createdWorkOrder = {
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
    workOrderApi.list.mockResolvedValue(pageResponse([]));
    operationalDecisionApi.listEligibleForWorkOrderCreation.mockResolvedValue(pageResponse([]));
    maintenanceActivityApi.list.mockResolvedValue([]);
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

    expect(await screen.findByRole('option', {
      name: /#1 — Central Playground \(Internal Maintenance\)/i,
    })).toBeInTheDocument();
    expect(operationalDecisionApi.listEligibleForWorkOrderCreation).toHaveBeenCalledWith(0, MAX_PAGE_SIZE);
  });

  it('does not show cross-department decisions when backend returns only eligible decisions', async () => {
    operationalDecisionApi.listEligibleForWorkOrderCreation.mockResolvedValue(
      pageResponse([eligibleDecision])
    );

    render(
      <MemoryRouter>
        <WorkOrdersPage />
      </MemoryRouter>
    );

    await screen.findByRole('option', {
      name: /#1 — Central Playground/i,
    });

    expect(screen.queryByRole('option', { name: /Other Department Asset/i })).not.toBeInTheDocument();
  });

  it('loads eligible assignees when work order is selected', async () => {
    const user = userEvent.setup();
    workOrderApi.list.mockResolvedValue(pageResponse([createdWorkOrder]));
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
      expect(screen.getByText('You do not have permission to create work orders.')).toBeInTheDocument();
    });
  });

  it('displays forbidden message when assignment is rejected', async () => {
    const user = userEvent.setup();
    workOrderApi.list.mockResolvedValue(pageResponse([createdWorkOrder]));
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
      expect(screen.getByText('You do not have permission to assign work orders.')).toBeInTheDocument();
    });
  });
});
