import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, waitFor, cleanup } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import BusinessTriggersPage from '../../pages/BusinessTriggersPage';
import businessTriggerApi from '../../services/businessTriggerApi';
import assetApi from '../../services/assetApi';
import userApi from '../../services/userApi';

const mockNavigate = vi.fn();

const { mockAuth, mockLogout } = vi.hoisted(() => ({
  mockAuth: { token: 'test-token', user: { userId: 1, role: 'MANAGER' } },
  mockLogout: vi.fn(),
}));

vi.mock('../../services/businessTriggerApi', () => ({
  default: {
    list: vi.fn(),
    create: vi.fn(),
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

describe('BusinessTriggersPage', () => {
  afterEach(cleanup);

  beforeEach(() => {
    vi.clearAllMocks();
    businessTriggerApi.list.mockResolvedValue(pageResponse([]));
    assetApi.list.mockResolvedValue(pageResponse([]));
    userApi.getCurrentUser.mockResolvedValue({ departmentId: 1 });
  });

  it('renders with mocked trigger and asset data', async () => {
    businessTriggerApi.list.mockResolvedValue(pageResponse([
      {
        id: 1,
        assetName: 'Central Playground',
        type: 'CUSTOMER_REQUEST',
        reason: 'Reported damage',
        urgent: false,
        createdAt: '2026-06-01T09:00:00',
      },
    ]));
    assetApi.list.mockResolvedValue(pageResponse([
      { id: 10, name: 'Central Playground', departmentId: 1, departmentName: 'Parks' },
      { id: 11, name: 'Other Asset', departmentId: 2, departmentName: 'Roads' },
    ]));

    render(
      <MemoryRouter>
        <BusinessTriggersPage />
      </MemoryRouter>
    );

    expect(await screen.findByText('Central Playground')).toBeInTheDocument();
    expect(screen.getByText('Reported damage')).toBeInTheDocument();
    expect(screen.getByRole('option', { name: 'Central Playground (Parks)' })).toBeInTheDocument();
    expect(screen.queryByRole('option', { name: 'Other Asset (Roads)' })).not.toBeInTheDocument();
  });

  it('renders empty list without crashing when assets return a Page object', async () => {
    render(
      <MemoryRouter>
        <BusinessTriggersPage />
      </MemoryRouter>
    );

    expect(await screen.findByText('No business triggers yet.')).toBeInTheDocument();
    expect(screen.getByText(/register at least one asset/i)).toBeInTheDocument();
  });

  it('displays API error message when loading fails', async () => {
    businessTriggerApi.list.mockRejectedValue({
      status: 500,
      message: 'Internal server error',
    });

    render(
      <MemoryRouter>
        <BusinessTriggersPage />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('Internal server error')).toBeInTheDocument();
    });
  });

  it('loads the first page on initial render', async () => {
    businessTriggerApi.list.mockResolvedValue(pageResponse([
      {
        id: 1,
        assetName: 'Page One Asset',
        type: 'CUSTOMER_REQUEST',
        reason: 'First page trigger',
        urgent: false,
        createdAt: '2026-06-01T09:00:00',
      },
    ], 0, 2));

    render(
      <MemoryRouter>
        <BusinessTriggersPage />
      </MemoryRouter>
    );

    expect(await screen.findByText('First page trigger')).toBeInTheDocument();
    expect(businessTriggerApi.list).toHaveBeenCalledWith(0);
  });

  it('loads the next page when Next is clicked', async () => {
    const user = userEvent.setup();
    businessTriggerApi.list.mockImplementation((page = 0) => {
      if (page === 0) {
        return Promise.resolve(pageResponse([
          {
            id: 1,
            assetName: 'Page One Asset',
            type: 'CUSTOMER_REQUEST',
            reason: 'First page trigger',
            urgent: false,
            createdAt: '2026-06-01T09:00:00',
          },
        ], 0, 2));
      }
      return Promise.resolve(pageResponse([
        {
          id: 2,
          assetName: 'Page Two Asset',
          type: 'CUSTOMER_REQUEST',
          reason: 'Second page trigger',
          urgent: false,
          createdAt: '2026-06-02T09:00:00',
        },
      ], 1, 2));
    });

    render(
      <MemoryRouter>
        <BusinessTriggersPage />
      </MemoryRouter>
    );

    expect(await screen.findByText('First page trigger')).toBeInTheDocument();
    await user.click(screen.getByTestId('pagination-next'));

    await waitFor(() => {
      expect(businessTriggerApi.list).toHaveBeenLastCalledWith(1);
      expect(screen.getByText('Second page trigger')).toBeInTheDocument();
    });
  });

  it('loads the previous page when Previous is clicked', async () => {
    const user = userEvent.setup();
    businessTriggerApi.list.mockImplementation((page = 0) => {
      if (page === 0) {
        return Promise.resolve(pageResponse([
          {
            id: 1,
            assetName: 'Page One Asset',
            type: 'CUSTOMER_REQUEST',
            reason: 'First page trigger',
            urgent: false,
            createdAt: '2026-06-01T09:00:00',
          },
        ], 0, 2));
      }
      return Promise.resolve(pageResponse([
        {
          id: 2,
          assetName: 'Page Two Asset',
          type: 'CUSTOMER_REQUEST',
          reason: 'Second page trigger',
          urgent: false,
          createdAt: '2026-06-02T09:00:00',
        },
      ], 1, 2));
    });

    render(
      <MemoryRouter>
        <BusinessTriggersPage />
      </MemoryRouter>
    );

    expect(await screen.findByText('First page trigger')).toBeInTheDocument();
    await user.click(screen.getByTestId('pagination-next'));
    expect(await screen.findByText('Second page trigger')).toBeInTheDocument();

    await user.click(screen.getByTestId('pagination-previous'));

    await waitFor(() => {
      expect(businessTriggerApi.list).toHaveBeenLastCalledWith(0);
      expect(screen.getByText('First page trigger')).toBeInTheDocument();
    });
  });
});
