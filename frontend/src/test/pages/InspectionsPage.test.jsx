import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, waitFor, cleanup } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import InspectionsPage from '../../pages/InspectionsPage';
import inspectionApi from '../../services/inspectionApi';
import businessTriggerApi from '../../services/businessTriggerApi';
import assetApi from '../../services/assetApi';
import userApi from '../../services/userApi';
import inspectionTemplateApi from '../../services/inspectionTemplateApi';
import { USER_ROLES } from '../../constants/userRoles';

const mockNavigate = vi.fn();

const { mockAuth } = vi.hoisted(() => ({
  mockAuth: { token: 'test-token', user: { userId: 10, role: 'OPERATIONAL_COORDINATOR' } },
}));

vi.mock('../../services/inspectionApi', () => ({
  default: {
    list: vi.fn(),
    assign: vi.fn(),
    listWorkers: vi.fn(),
  },
}));

vi.mock('../../services/businessTriggerApi', () => ({
  default: {
    list: vi.fn(),
  },
}));

vi.mock('../../services/assetApi', () => ({
  default: {
    list: vi.fn(),
  },
}));

vi.mock('../../services/userApi', () => ({
  default: {
    getCurrentUser: vi.fn(),
  },
}));

vi.mock('../../services/inspectionTemplateApi', () => ({
  default: {
    list: vi.fn(),
  },
}));

vi.mock('../../services/inspectionTemplateQuestionApi', () => ({
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
    logout: vi.fn(),
  }),
}));

vi.mock('../../components/NotificationButton', () => ({
  default: () => <button type="button">Notifications</button>,
}));

function pageResponse(content, number = 0, totalPages = 1) {
  return { content, number, totalPages };
}

const assets = [
  { id: 5, name: 'Street Light A', departmentId: 1, assetCategoryId: 20 },
];

const triggers = [
  {
    id: 1,
    assetId: 5,
    assetName: 'Street Light A',
    type: 'CUSTOMER_REQUEST',
    reason: 'Routine check',
    urgent: false,
  },
];

const workers = [
  { id: 20, name: 'Alex Field', role: USER_ROLES.FIELD_EMPLOYEE, status: 'ACTIVE', departmentId: 1 },
];

const publishedTemplate = {
  id: 50,
  name: 'Street Light Inspection',
  version: 1,
  status: 'PUBLISHED',
  assetCategoryId: 20,
};

describe('InspectionsPage template assignment', () => {
  afterEach(cleanup);

  beforeEach(() => {
    vi.clearAllMocks();
    mockAuth.user = { userId: 10, role: USER_ROLES.OPERATIONAL_COORDINATOR };
    inspectionApi.list.mockResolvedValue(pageResponse([]));
    businessTriggerApi.list.mockResolvedValue(pageResponse(triggers));
    assetApi.list.mockResolvedValue(pageResponse(assets));
    userApi.getCurrentUser.mockResolvedValue({ departmentId: 1 });
    inspectionApi.listWorkers.mockResolvedValue(workers);
    inspectionTemplateApi.list.mockResolvedValue(pageResponse([publishedTemplate]));
    inspectionApi.assign.mockResolvedValue({ id: 100 });
  });

  it('loads only published templates for the selected asset category', async () => {
    const user = userEvent.setup();

    render(
      <MemoryRouter>
        <InspectionsPage />
      </MemoryRouter>
    );

    await screen.findByLabelText('Business Trigger');
    await user.selectOptions(screen.getByLabelText('Business Trigger'), '1');

    await waitFor(() => {
      expect(inspectionTemplateApi.list).toHaveBeenCalledWith(
        0,
        100,
        { assetCategoryId: 20, status: 'PUBLISHED' }
      );
    });

    expect(await screen.findByLabelText('Inspection Template')).toBeInTheDocument();
    expect(screen.getByRole('option', { name: 'Street Light Inspection (v1)' })).toBeInTheDocument();
  });

  it('assigns inspection without template for legacy workflow', async () => {
    const user = userEvent.setup();

    render(
      <MemoryRouter>
        <InspectionsPage />
      </MemoryRouter>
    );

    await screen.findByLabelText('Business Trigger');
    await user.selectOptions(screen.getByLabelText('Business Trigger'), '1');
    await user.selectOptions(screen.getByLabelText('Assign To'), '20');
    await user.click(screen.getByRole('button', { name: 'Assign Inspection' }));

    await waitFor(() => {
      expect(inspectionApi.assign).toHaveBeenCalledWith({
        businessTriggerId: 1,
        assignedToUserId: 20,
        priority: 'NORMAL',
      });
    });
  });

  it('assigns inspection with selected published template', async () => {
    const user = userEvent.setup();

    render(
      <MemoryRouter>
        <InspectionsPage />
      </MemoryRouter>
    );

    await screen.findByLabelText('Business Trigger');
    await user.selectOptions(screen.getByLabelText('Business Trigger'), '1');
    await user.selectOptions(screen.getByLabelText('Assign To'), '20');
    await user.selectOptions(screen.getByLabelText('Inspection Template'), '50');
    await user.click(screen.getByRole('button', { name: 'Assign Inspection' }));

    await waitFor(() => {
      expect(inspectionApi.assign).toHaveBeenCalledWith({
        businessTriggerId: 1,
        assignedToUserId: 20,
        priority: 'NORMAL',
        inspectionTemplateId: 50,
      });
    });
  });
});
