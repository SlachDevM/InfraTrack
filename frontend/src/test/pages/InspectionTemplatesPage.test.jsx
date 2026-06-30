import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, waitFor, cleanup } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import InspectionTemplatesPage from '../../pages/InspectionTemplatesPage';
import inspectionTemplateApi from '../../services/inspectionTemplateApi';
import assetCategoryApi from '../../services/assetCategoryApi';
import { USER_ROLES } from '../../constants/userRoles';

const mockNavigate = vi.fn();

const { mockAuth, mockLogout } = vi.hoisted(() => ({
  mockAuth: { token: 'test-token', user: { userId: 1, role: 'ADMINISTRATOR' } },
  mockLogout: vi.fn(),
}));

vi.mock('../../services/inspectionTemplateApi', () => ({
  default: {
    list: vi.fn(),
    create: vi.fn(),
    update: vi.fn(),
    archive: vi.fn(),
    publish: vi.fn(),
  },
}));

vi.mock('../../services/assetCategoryApi', () => ({
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

const categories = [{ id: 10, name: 'Pump' }];

const template = {
  id: 100,
  name: 'Pump Inspection Template',
  description: 'Standard pump inspection',
  assetCategoryId: 10,
  assetCategoryName: 'Pump',
  version: 1,
  status: 'DRAFT',
  updatedAt: 1710000000000,
};

describe('InspectionTemplatesPage', () => {
  afterEach(cleanup);

  beforeEach(() => {
    vi.clearAllMocks();
    mockAuth.user.role = 'ADMINISTRATOR';
    inspectionTemplateApi.list.mockResolvedValue(pageResponse([template]));
    assetCategoryApi.list.mockResolvedValue(categories);
    window.confirm = vi.fn(() => true);
  });

  it('renders template list for administrator', async () => {
    const { container } = render(
      <MemoryRouter>
        <InspectionTemplatesPage />
      </MemoryRouter>
    );

    expect(await screen.findByText('Pump Inspection Template')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Create Template' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Edit' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Publish' })).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Archive' })).not.toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Manage Questions' })).toBeInTheDocument();
    expect(container.querySelector('.reference-header')).toBeInTheDocument();
    expect(container.querySelector('.reference-content')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: '← Back' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Notifications' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Logout' })).toBeInTheDocument();
  });

  it('shows read-only view for manager', async () => {
    mockAuth.user.role = USER_ROLES.MANAGER;

    render(
      <MemoryRouter>
        <InspectionTemplatesPage />
      </MemoryRouter>
    );

    expect(await screen.findByText('Pump Inspection Template')).toBeInTheDocument();
    expect(screen.getByText(/read-only/i)).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Create Template' })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Edit' })).not.toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Manage Questions' })).toBeInTheDocument();
  });

  it('shows Publish only for draft templates', async () => {
    render(
      <MemoryRouter>
        <InspectionTemplatesPage />
      </MemoryRouter>
    );

    expect(await screen.findByText('Pump Inspection Template')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Publish' })).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Archive' })).not.toBeInTheDocument();
  });

  it('shows Archive and View Questions only for published templates', async () => {
    inspectionTemplateApi.list.mockResolvedValue(pageResponse([
      { ...template, status: 'PUBLISHED' },
    ]));

    render(
      <MemoryRouter>
        <InspectionTemplatesPage />
      </MemoryRouter>
    );

    expect(await screen.findByText('Pump Inspection Template')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'View Questions' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Archive' })).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Publish' })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Edit' })).not.toBeInTheDocument();
  });

  it('shows View Questions only for archived templates', async () => {
    inspectionTemplateApi.list.mockResolvedValue(pageResponse([
      { ...template, status: 'ARCHIVED' },
    ]));

    render(
      <MemoryRouter>
        <InspectionTemplatesPage />
      </MemoryRouter>
    );

    expect(await screen.findByText('Pump Inspection Template')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'View Questions' })).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Publish' })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Archive' })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Edit' })).not.toBeInTheDocument();
  });

  it('publishes template when publish action is confirmed', async () => {
    const user = userEvent.setup();
    inspectionTemplateApi.publish.mockResolvedValue({ ...template, status: 'PUBLISHED' });

    render(
      <MemoryRouter>
        <InspectionTemplatesPage />
      </MemoryRouter>
    );

    await screen.findByText('Pump Inspection Template');
    await user.click(screen.getByRole('button', { name: 'Publish' }));

    await waitFor(() => {
      expect(inspectionTemplateApi.publish).toHaveBeenCalledWith(100);
      expect(screen.getByText('Inspection template published.')).toBeInTheDocument();
    });
  });

  it('shows read-only view for operational coordinator', async () => {
    mockAuth.user.role = USER_ROLES.OPERATIONAL_COORDINATOR;

    render(
      <MemoryRouter>
        <InspectionTemplatesPage />
      </MemoryRouter>
    );

    expect(await screen.findByText('Pump Inspection Template')).toBeInTheDocument();
    expect(screen.getByText(/read-only/i)).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Create Template' })).not.toBeInTheDocument();
  });

  it('redirects field employee away from page', async () => {
    mockAuth.user.role = USER_ROLES.FIELD_EMPLOYEE;

    render(
      <MemoryRouter>
        <InspectionTemplatesPage />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith('/');
    });
  });

  it('submits create form with expected payload', async () => {
    const user = userEvent.setup();
    inspectionTemplateApi.create.mockResolvedValue({ id: 101 });

    render(
      <MemoryRouter>
        <InspectionTemplatesPage />
      </MemoryRouter>
    );

    await screen.findByText('Pump Inspection Template');
    await user.type(screen.getByLabelText('Name'), 'Motor Inspection Template');
    await user.selectOptions(document.getElementById('assetCategoryId'), '10');
    await user.click(screen.getByRole('button', { name: 'Create Template' }));

    await waitFor(() => {
      expect(inspectionTemplateApi.create).toHaveBeenCalledWith({
        name: 'Motor Inspection Template',
        assetCategoryId: 10,
      });
    });
  });

  it('loads next page when pagination next is clicked', async () => {
    const user = userEvent.setup();
    inspectionTemplateApi.list.mockImplementation((page = 0) => {
      if (page === 0) {
        return Promise.resolve(pageResponse([template], 0, 2));
      }
      return Promise.resolve(pageResponse([
        { ...template, id: 101, name: 'Second Template' },
      ], 1, 2));
    });

    render(
      <MemoryRouter>
        <InspectionTemplatesPage />
      </MemoryRouter>
    );

    expect(await screen.findByText('Pump Inspection Template')).toBeInTheDocument();
    await user.click(screen.getByTestId('pagination-next'));

    await waitFor(() => {
      expect(inspectionTemplateApi.list).toHaveBeenLastCalledWith(1, undefined, expect.any(Object));
      expect(screen.getByText('Second Template')).toBeInTheDocument();
    });
  });

  it('applies category filter when selected', async () => {
    const user = userEvent.setup();

    render(
      <MemoryRouter>
        <InspectionTemplatesPage />
      </MemoryRouter>
    );

    await screen.findByText('Pump Inspection Template');
    await user.selectOptions(screen.getByLabelText('Asset Category', { selector: '#filterAssetCategoryId' }), '10');

    await waitFor(() => {
      expect(inspectionTemplateApi.list).toHaveBeenCalledWith(
        0,
        undefined,
        expect.objectContaining({ assetCategoryId: 10 })
      );
    });
  });

  it('applies status filter when selected', async () => {
    const user = userEvent.setup();

    render(
      <MemoryRouter>
        <InspectionTemplatesPage />
      </MemoryRouter>
    );

    await screen.findByText('Pump Inspection Template');
    await user.selectOptions(screen.getByLabelText('Status', { selector: '#filterStatus' }), 'DRAFT');

    await waitFor(() => {
      expect(inspectionTemplateApi.list).toHaveBeenCalledWith(
        0,
        undefined,
        expect.objectContaining({ status: 'DRAFT' })
      );
    });
  });

  it('archives published template when archive action is confirmed', async () => {
    const user = userEvent.setup();
    inspectionTemplateApi.list.mockResolvedValue(pageResponse([
      { ...template, status: 'PUBLISHED' },
    ]));
    inspectionTemplateApi.archive.mockResolvedValue({ ...template, status: 'ARCHIVED' });

    render(
      <MemoryRouter>
        <InspectionTemplatesPage />
      </MemoryRouter>
    );

    await screen.findByText('Pump Inspection Template');
    await user.click(screen.getByRole('button', { name: 'Archive' }));

    await waitFor(() => {
      expect(inspectionTemplateApi.archive).toHaveBeenCalledWith(100);
      expect(screen.getByText('Inspection template archived.')).toBeInTheDocument();
    });
  });
});
