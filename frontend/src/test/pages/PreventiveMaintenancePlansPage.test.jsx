import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, waitFor, cleanup } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import PreventiveMaintenancePlansPage from '../../pages/PreventiveMaintenancePlansPage';
import preventiveMaintenancePlanApi from '../../services/preventiveMaintenancePlanApi';
import assetApi from '../../services/assetApi';
import inspectionTemplateApi from '../../services/inspectionTemplateApi';
import { USER_ROLES } from '../../constants/userRoles';

const mockNavigate = vi.fn();

const { mockAuth, mockLogout } = vi.hoisted(() => ({
  mockAuth: { token: 'test-token', user: { userId: 1, role: 'ADMINISTRATOR' } },
  mockLogout: vi.fn(),
}));

vi.mock('../../services/preventiveMaintenancePlanApi', () => ({
  default: {
    list: vi.fn(),
    get: vi.fn(),
    create: vi.fn(),
    update: vi.fn(),
    archive: vi.fn(),
    evaluate: vi.fn(),
    evaluateAll: vi.fn(),
  },
}));

vi.mock('../../services/assetApi', () => ({
  default: {
    list: vi.fn(),
  },
}));

vi.mock('../../services/inspectionTemplateApi', () => ({
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

const assets = [{ id: 5, name: 'Pump A' }];
const templates = [{ id: 20, name: 'Pump Monthly Inspection' }];

const plan = {
  id: 100,
  planCode: 'PUMP_MONTHLY',
  version: 1,
  name: 'Monthly Pump Inspection',
  description: 'Monthly preventive inspection',
  assetId: 5,
  assetName: 'Pump A',
  status: 'ACTIVE',
  priority: 'MEDIUM',
  targetAction: 'CREATE_INSPECTION',
  inspectionTemplateId: 20,
  inspectionTemplateName: 'Pump Monthly Inspection',
  businessTrigger: {
    id: 200,
    triggerType: 'TIME',
    configurationJson: '{"every":1,"unit":"MONTH"}',
    triggerSummary: {
      title: 'Every month',
      description: 'Eligible once every full month from plan creation.',
      triggerType: 'TIME',
    },
    active: true,
    createdAt: 1710000000000,
    updatedAt: 1710000000000,
  },
  updatedAt: 1710000000000,
};

describe('PreventiveMaintenancePlansPage', () => {
  afterEach(cleanup);

  beforeEach(() => {
    vi.clearAllMocks();
    mockAuth.user.role = 'ADMINISTRATOR';
    preventiveMaintenancePlanApi.list.mockResolvedValue(pageResponse([plan]));
    preventiveMaintenancePlanApi.get.mockResolvedValue(plan);
    assetApi.list.mockResolvedValue(pageResponse(assets));
    inspectionTemplateApi.list.mockResolvedValue(pageResponse(templates));
    window.confirm = vi.fn(() => true);
  });

  it('renders plan list with trigger summary for administrator', async () => {
    render(
      <MemoryRouter>
        <PreventiveMaintenancePlansPage />
      </MemoryRouter>
    );

    expect(await screen.findByText('PUMP_MONTHLY')).toBeInTheDocument();
    expect(screen.getByRole('cell', { name: 'Every month' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Edit' })).toBeInTheDocument();
  });

  it('shows read-only view for manager', async () => {
    mockAuth.user.role = USER_ROLES.MANAGER;

    render(
      <MemoryRouter>
        <PreventiveMaintenancePlansPage />
      </MemoryRouter>
    );

    expect(await screen.findByText('PUMP_MONTHLY')).toBeInTheDocument();
    expect(screen.getByText(/read-only/i)).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Create Plan' })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Edit' })).not.toBeInTheDocument();
  });

  it('shows structured TIME trigger fields and summary preview', async () => {
    render(
      <MemoryRouter>
        <PreventiveMaintenancePlansPage />
      </MemoryRouter>
    );

    await screen.findByText('PUMP_MONTHLY');
    expect(screen.getByLabelText(/^Every$/)).toBeInTheDocument();
    expect(screen.getByLabelText(/^Unit$/)).toBeInTheDocument();
    expect(screen.getByText(/Trigger summary:/i)).toBeInTheDocument();
    expect(screen.queryByLabelText(/Trigger Configuration/i)).not.toBeInTheDocument();
    expect(screen.queryByLabelText(/Trigger Configuration/i)).not.toBeInTheDocument();
  });

  it('creates a preventive maintenance plan with plan code', async () => {
    const user = userEvent.setup();
    preventiveMaintenancePlanApi.create.mockResolvedValue(plan);

    render(
      <MemoryRouter>
        <PreventiveMaintenancePlansPage />
      </MemoryRouter>
    );

    await screen.findByText('PUMP_MONTHLY');

    await user.type(screen.getByLabelText(/^Plan Code$/), 'NEW_PLAN');
    await user.type(screen.getByLabelText(/^Name$/), 'New Plan');
    const assetSelects = screen.getAllByLabelText('Asset');
    await user.selectOptions(assetSelects[assetSelects.length - 1], '5');
    await user.click(screen.getByRole('button', { name: 'Create Plan' }));

    await waitFor(() => {
      expect(preventiveMaintenancePlanApi.create).toHaveBeenCalled();
    });
    const payload = preventiveMaintenancePlanApi.create.mock.calls[0][0];
    expect(payload.planCode).toBe('NEW_PLAN');
    expect(payload.businessTrigger.configurationJson).toBe('{"every":1,"unit":"MONTH"}');
  });

  it('rejects invalid plan code before submit', async () => {
    const user = userEvent.setup();

    render(
      <MemoryRouter>
        <PreventiveMaintenancePlansPage />
      </MemoryRouter>
    );

    await screen.findByText('PUMP_MONTHLY');
    await user.type(screen.getByLabelText(/^Plan Code$/), '1INVALID');
    await user.type(screen.getByLabelText(/^Name$/), 'Invalid Plan');
    const assetSelects = screen.getAllByLabelText('Asset');
    await user.selectOptions(assetSelects[assetSelects.length - 1], '5');
    await user.click(screen.getByRole('button', { name: 'Create Plan' }));

    expect(await screen.findByText(/uppercase snake_case/i)).toBeInTheDocument();
    expect(preventiveMaintenancePlanApi.create).not.toHaveBeenCalled();
  });

  it('archives a plan', async () => {
    const user = userEvent.setup();
    preventiveMaintenancePlanApi.archive.mockResolvedValue({ ...plan, status: 'ARCHIVED' });

    render(
      <MemoryRouter>
        <PreventiveMaintenancePlansPage />
      </MemoryRouter>
    );

    await screen.findByText('PUMP_MONTHLY');
    await user.click(screen.getByRole('button', { name: 'Archive' }));

    await waitFor(() => {
      expect(preventiveMaintenancePlanApi.archive).toHaveBeenCalledWith(100);
    });
  });

  it('evaluates plan and shows eligibility result', async () => {
    const user = userEvent.setup();
    preventiveMaintenancePlanApi.evaluate.mockResolvedValue({
      planId: 100,
      planCode: 'PUMP_MONTHLY',
      triggerType: 'TIME',
      eligible: true,
      evaluationReason: 'One full month has elapsed.',
      evaluatedAt: 1710000000000,
      evaluationDurationMs: 1,
      triggerSummary: plan.businessTrigger.triggerSummary,
    });

    render(
      <MemoryRouter>
        <PreventiveMaintenancePlansPage />
      </MemoryRouter>
    );

    await screen.findByText('PUMP_MONTHLY');
    await user.click(screen.getByRole('button', { name: 'Evaluate' }));

    expect(await screen.findByText('Eligible')).toBeInTheDocument();
    expect(screen.getByText('One full month has elapsed.')).toBeInTheDocument();
    expect(screen.getByText(/Evaluated:/)).toBeInTheDocument();
    expect(preventiveMaintenancePlanApi.evaluate).toHaveBeenCalledWith(100);
  });

  it('loads plan details when editing', async () => {
    const user = userEvent.setup();

    render(
      <MemoryRouter>
        <PreventiveMaintenancePlansPage />
      </MemoryRouter>
    );

    await screen.findByText('PUMP_MONTHLY');
    await user.click(screen.getByRole('button', { name: 'Edit' }));

    await waitFor(() => {
      expect(preventiveMaintenancePlanApi.get).toHaveBeenCalledWith(100);
    });
    expect(screen.getByRole('button', { name: 'Update Plan' })).toBeInTheDocument();
    expect(screen.getByLabelText(/^Plan Code$/)).toBeDisabled();
  });
});
