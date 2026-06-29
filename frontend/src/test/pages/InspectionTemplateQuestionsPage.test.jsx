import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, waitFor, cleanup } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import InspectionTemplateQuestionsPage from '../../pages/InspectionTemplateQuestionsPage';
import inspectionTemplateApi from '../../services/inspectionTemplateApi';
import inspectionTemplateQuestionApi from '../../services/inspectionTemplateQuestionApi';
import unitOfMeasureApi from '../../services/unitOfMeasureApi';
import { USER_ROLES } from '../../constants/userRoles';

const mockNavigate = vi.fn();

const { mockAuth, mockLogout } = vi.hoisted(() => ({
  mockAuth: { token: 'test-token', user: { userId: 1, role: 'ADMINISTRATOR' } },
  mockLogout: vi.fn(),
}));

vi.mock('../../services/inspectionTemplateApi', () => ({
  default: {
    get: vi.fn(),
  },
}));

vi.mock('../../services/inspectionTemplateQuestionApi', () => ({
  default: {
    list: vi.fn(),
    create: vi.fn(),
    update: vi.fn(),
    deactivate: vi.fn(),
    reorder: vi.fn(),
  },
}));

vi.mock('../../services/unitOfMeasureApi', () => ({
  default: {
    list: vi.fn(),
  },
}));

vi.mock('../../services/inspectionTemplateQuestionChoiceApi', () => ({
  default: {
    list: vi.fn(),
    create: vi.fn(),
    update: vi.fn(),
    deactivate: vi.fn(),
    reorder: vi.fn(),
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

const draftTemplate = {
  id: 100,
  name: 'Pump Inspection Template',
  assetCategoryName: 'Pump',
  version: 1,
  status: 'DRAFT',
};

const publishedTemplate = {
  ...draftTemplate,
  status: 'PUBLISHED',
};

const questions = [
  {
    id: 1,
    inspectionTemplateId: 100,
    code: 'VISIBLE_LEAK',
    questionText: 'Is there any visible leak?',
    helpText: null,
    questionType: 'BOOLEAN',
    required: false,
    displayOrder: 1,
    active: true,
  },
  {
    id: 2,
    inspectionTemplateId: 100,
    code: 'DESCRIBE_VIBRATION',
    questionText: 'Describe vibration',
    helpText: 'Optional detail',
    questionType: 'TEXT',
    required: true,
    displayOrder: 2,
    active: true,
  },
];

function renderPage(templateId = '100') {
  return render(
    <MemoryRouter initialEntries={[`/inspection-templates/${templateId}/questions`]}>
      <Routes>
        <Route
          path="/inspection-templates/:templateId/questions"
          element={<InspectionTemplateQuestionsPage />}
        />
      </Routes>
    </MemoryRouter>
  );
}

describe('InspectionTemplateQuestionsPage', () => {
  afterEach(cleanup);

  beforeEach(() => {
    vi.clearAllMocks();
    mockAuth.user.role = 'ADMINISTRATOR';
    inspectionTemplateApi.get.mockResolvedValue(draftTemplate);
    inspectionTemplateQuestionApi.list.mockResolvedValue(questions);
    unitOfMeasureApi.list.mockResolvedValue([
      { id: 1, code: 'CELSIUS', symbol: '°C', name: 'Celsius', quantityType: 'TEMPERATURE', active: true },
    ]);
    window.confirm = vi.fn(() => true);
  });

  it('renders question list for administrator', async () => {
    renderPage();

    expect(await screen.findByText('Is there any visible leak?')).toBeInTheDocument();
    expect(screen.getByText('VISIBLE_LEAK')).toBeInTheDocument();
    expect(screen.getByText('Describe vibration')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Create Question' })).toBeInTheDocument();
  });

  it('suggests code automatically when creating a question', async () => {
    const user = userEvent.setup();
    renderPage();

    await screen.findByText('Is there any visible leak?');
    await user.type(screen.getByLabelText('Question Text'), 'Is abnormal vibration present?');

    expect(screen.getByLabelText('Question Code')).toHaveValue('IS_ABNORMAL_VIBRATION_PRESENT');
  });

  it('allows administrator to edit suggested code before save', async () => {
    const user = userEvent.setup();
    inspectionTemplateQuestionApi.create.mockResolvedValue({
      id: 3,
      code: 'VIBRATION',
      questionText: 'Is abnormal vibration present?',
      questionType: 'BOOLEAN',
      required: false,
      displayOrder: 3,
      active: true,
    });
    inspectionTemplateQuestionApi.list.mockResolvedValueOnce(questions).mockResolvedValueOnce(questions);

    renderPage();
    await screen.findByText('Is there any visible leak?');
    await user.type(screen.getByLabelText('Question Text'), 'Is abnormal vibration present?');

    const codeInput = screen.getByLabelText('Question Code');
    await user.clear(codeInput);
    await user.type(codeInput, 'VIBRATION');
    await user.click(screen.getByRole('button', { name: 'Create Question' }));

    await waitFor(() => {
      expect(inspectionTemplateQuestionApi.create).toHaveBeenCalledWith('100', {
        code: 'VIBRATION',
        questionText: 'Is abnormal vibration present?',
        helpText: undefined,
        questionType: 'BOOLEAN',
        required: false,
      });
    });
  });

  it('admin can create question', async () => {
    const user = userEvent.setup();
    inspectionTemplateQuestionApi.create.mockResolvedValue({
      id: 3,
      code: 'IS_CORROSION_VISIBLE',
      questionText: 'Is corrosion visible?',
      questionType: 'BOOLEAN',
      required: false,
      displayOrder: 3,
      active: true,
    });
    inspectionTemplateQuestionApi.list.mockResolvedValueOnce(questions).mockResolvedValueOnce([
      ...questions,
      {
        id: 3,
        code: 'IS_CORROSION_VISIBLE',
        questionText: 'Is corrosion visible?',
        questionType: 'BOOLEAN',
        required: false,
        displayOrder: 3,
        active: true,
      },
    ]);

    renderPage();

    await screen.findByText('Is there any visible leak?');
    await user.type(screen.getByLabelText('Question Text'), 'Is corrosion visible?');
    await user.click(screen.getByRole('button', { name: 'Create Question' }));

    await waitFor(() => {
      expect(inspectionTemplateQuestionApi.create).toHaveBeenCalledWith('100', {
        code: 'IS_CORROSION_VISIBLE',
        questionText: 'Is corrosion visible?',
        helpText: undefined,
        questionType: 'BOOLEAN',
        required: false,
      });
    });
  });

  it('shows code as read-only when editing', async () => {
    const user = userEvent.setup();
    renderPage();
    await screen.findByText('Is there any visible leak?');
    await user.click(screen.getAllByRole('button', { name: 'Edit' })[0]);

    const codeInput = screen.getByLabelText('Question Code');
    expect(codeInput).toHaveValue('VISIBLE_LEAK');
    expect(codeInput).toBeDisabled();
  });

  it('rejects invalid code before submit', async () => {
    const user = userEvent.setup();
    renderPage();
    await screen.findByText('Is there any visible leak?');
    await user.type(screen.getByLabelText('Question Text'), 'Bad code example');
    const codeInput = screen.getByLabelText('Question Code');
    await user.clear(codeInput);
    await user.type(codeInput, 'bad-code');
    await user.click(screen.getByRole('button', { name: 'Create Question' }));

    expect(await screen.findByText(/Question code must be uppercase/i)).toBeInTheDocument();
    expect(inspectionTemplateQuestionApi.create).not.toHaveBeenCalled();
  });

  it('admin can edit question', async () => {
    const user = userEvent.setup();
    inspectionTemplateQuestionApi.update.mockResolvedValue({
      ...questions[0],
      questionText: 'Updated leak question',
    });

    renderPage();
    await screen.findByText('Is there any visible leak?');
    await user.click(screen.getAllByRole('button', { name: 'Edit' })[0]);

    const input = screen.getByLabelText('Question Text');
    await user.clear(input);
    await user.type(input, 'Updated leak question');
    await user.click(screen.getByRole('button', { name: 'Update Question' }));

    await waitFor(() => {
      expect(inspectionTemplateQuestionApi.update).toHaveBeenCalledWith('100', 1, {
        questionText: 'Updated leak question',
        helpText: undefined,
        questionType: 'BOOLEAN',
        required: false,
      });
    });
  });

  it('admin can deactivate question', async () => {
    const user = userEvent.setup();
    inspectionTemplateQuestionApi.deactivate.mockResolvedValue({
      ...questions[0],
      active: false,
    });

    renderPage();
    await screen.findByText('Is there any visible leak?');
    await user.click(screen.getAllByRole('button', { name: 'Deactivate' })[0]);

    await waitFor(() => {
      expect(inspectionTemplateQuestionApi.deactivate).toHaveBeenCalledWith('100', 1);
    });
  });

  it('admin can reorder question', async () => {
    const user = userEvent.setup();
    inspectionTemplateQuestionApi.reorder.mockResolvedValue([
      { ...questions[1], displayOrder: 1 },
      { ...questions[0], displayOrder: 2 },
    ]);

    renderPage();
    await screen.findByText('Is there any visible leak?');
    await user.click(screen.getAllByRole('button', { name: 'Move Down' })[0]);

    await waitFor(() => {
      expect(inspectionTemplateQuestionApi.reorder).toHaveBeenCalledWith('100', [2, 1]);
    });
  });

  it('shows read-only view for manager', async () => {
    mockAuth.user.role = USER_ROLES.MANAGER;

    renderPage();

    expect(await screen.findByText('Is there any visible leak?')).toBeInTheDocument();
    expect(screen.getByText(/read-only/i)).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Create Question' })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Edit' })).not.toBeInTheDocument();
  });

  it('shows read-only view for operational coordinator', async () => {
    mockAuth.user.role = USER_ROLES.OPERATIONAL_COORDINATOR;

    renderPage();

    expect(await screen.findByText('Is there any visible leak?')).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Create Question' })).not.toBeInTheDocument();
  });

  it('redirects field employee away from page', async () => {
    mockAuth.user.role = USER_ROLES.FIELD_EMPLOYEE;

    renderPage();

    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith('/');
    });
  });

  it('disables mutation controls for published template', async () => {
    inspectionTemplateApi.get.mockResolvedValue(publishedTemplate);

    renderPage();

    expect(await screen.findByText(/cannot be modified/i)).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Create Question' })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Edit' })).not.toBeInTheDocument();
  });

  it('renders question types correctly', async () => {
    renderPage();

    expect(await screen.findByText('Is there any visible leak?')).toBeInTheDocument();
    expect(screen.getByText('Describe vibration')).toBeInTheDocument();
    expect(screen.getAllByText('Boolean (Yes/No)').length).toBeGreaterThan(0);
    expect(screen.getAllByText('Text').length).toBeGreaterThan(0);
  });

  it('shows unit dropdown only for NUMBER questions', async () => {
    const user = userEvent.setup();
    renderPage();

    await screen.findByText('Is there any visible leak?');
    expect(screen.queryByLabelText('Unit of Measure')).not.toBeInTheDocument();

    await user.selectOptions(screen.getByLabelText('Question Type'), 'NUMBER');
    expect(screen.getByLabelText('Unit of Measure')).toBeInTheDocument();
    expect(screen.getByText('°C — Celsius (TEMPERATURE)')).toBeInTheDocument();
  });
});
