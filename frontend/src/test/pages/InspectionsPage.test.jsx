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
import inspectionTemplateQuestionApi from '../../services/inspectionTemplateQuestionApi';
import { USER_ROLES } from '../../constants/userRoles';

const mockNavigate = vi.fn();

const { mockAuth } = vi.hoisted(() => ({
  mockAuth: { token: 'test-token', user: { userId: 10, role: 'OPERATIONAL_COORDINATOR' } },
}));

vi.mock('../../services/inspectionApi', () => ({
  default: {
    list: vi.fn(),
    assign: vi.fn(),
    complete: vi.fn(),
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

const assets = [{ id: 5, name: 'Street Light A', departmentId: 1, assetCategoryId: 20 }];

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
  {
    id: 20,
    name: 'Alex Field',
    role: USER_ROLES.FIELD_EMPLOYEE,
    status: 'ACTIVE',
    departmentId: 1,
  },
];

const publishedTemplate = {
  id: 50,
  name: 'Street Light Inspection',
  version: 1,
  status: 'PUBLISHED',
  assetCategoryId: 20,
};

const templateQuestions = [
  {
    id: 1,
    code: 'LIGHT_WORKING',
    questionText: 'Is the light working?',
    questionType: 'BOOLEAN',
    required: true,
    active: true,
  },
  {
    id: 2,
    code: 'POLE_DAMAGE',
    questionText: 'Pole damage level',
    questionType: 'CHOICE',
    required: false,
    active: true,
    choices: [
      { id: 10, code: 'NONE', label: 'None', displayOrder: 1, active: true },
      { id: 11, code: 'MINOR', label: 'Minor', displayOrder: 2, active: true },
    ],
  },
  {
    id: 3,
    code: 'NOTES',
    questionText: 'Additional notes',
    questionType: 'TEXT',
    required: false,
    active: true,
  },
];

const templatedInspection = {
  id: 200,
  status: 'ASSIGNED',
  assignedToUserId: 20,
  assetName: 'Street Light A',
  businessTriggerId: 1,
  businessTriggerType: 'CUSTOMER_REQUEST',
  businessTriggerReason: 'Routine check',
  inspectionTemplateId: 50,
  inspectionTemplateName: 'Street Light Inspection',
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
      expect(inspectionTemplateApi.list).toHaveBeenCalledWith(0, 100, {
        assetCategoryId: 20,
        status: 'PUBLISHED',
      });
    });

    expect(await screen.findByLabelText('Inspection Template')).toBeInTheDocument();
    expect(
      screen.getByRole('option', { name: 'Street Light Inspection (v1)' })
    ).toBeInTheDocument();
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

describe('InspectionsPage templated inspection completion', () => {
  afterEach(cleanup);

  beforeEach(() => {
    vi.clearAllMocks();
    mockAuth.user = { userId: 20, role: USER_ROLES.FIELD_EMPLOYEE };
    inspectionApi.list.mockResolvedValue(pageResponse([templatedInspection]));
    businessTriggerApi.list.mockResolvedValue(pageResponse([]));
    inspectionTemplateQuestionApi.list.mockResolvedValue(templateQuestions);
    inspectionApi.complete.mockResolvedValue({ id: 200, status: 'COMPLETED' });
  });

  it('loads checklist questions for assigned templated inspection', async () => {
    render(
      <MemoryRouter>
        <InspectionsPage />
      </MemoryRouter>
    );

    expect(await screen.findByLabelText(/Is the light working/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Pole damage level/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Additional notes/i)).toBeInTheDocument();
    expect(
      screen.queryByText(/No active checklist questions are defined/i)
    ).not.toBeInTheDocument();
    expect(inspectionTemplateQuestionApi.list).toHaveBeenCalledWith(50);
  });

  it('submits LIGHT_WORKING=false with structured answers', async () => {
    const user = userEvent.setup();

    render(
      <MemoryRouter>
        <InspectionsPage />
      </MemoryRouter>
    );

    await screen.findByLabelText(/Is the light working/i);
    await user.selectOptions(screen.getByLabelText(/Is the light working/i), 'false');
    await user.type(screen.getByLabelText(/Observations/i), 'Light is out.');
    await user.click(screen.getByRole('button', { name: 'Complete Inspection' }));

    await waitFor(() => {
      expect(inspectionApi.complete).toHaveBeenCalledWith(
        200,
        expect.objectContaining({
          answers: [
            expect.objectContaining({
              questionId: 1,
              booleanValue: false,
            }),
          ],
        })
      );
    });
  });

  it('keeps legacy completion form when inspection has no template', async () => {
    inspectionApi.list.mockResolvedValue(
      pageResponse([
        {
          ...templatedInspection,
          id: 201,
          inspectionTemplateId: null,
          inspectionTemplateName: null,
        },
      ])
    );

    render(
      <MemoryRouter>
        <InspectionsPage />
      </MemoryRouter>
    );

    await screen.findByLabelText(/Observations/i);
    expect(screen.queryByText(/Checklist Questions/i)).not.toBeInTheDocument();
    expect(inspectionTemplateQuestionApi.list).not.toHaveBeenCalled();
  });
});
