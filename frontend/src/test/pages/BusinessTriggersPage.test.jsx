import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, waitFor, cleanup } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import BusinessTriggersPage from '../../pages/BusinessTriggersPage';
import businessTriggerApi from '../../services/businessTriggerApi';
import assetApi from '../../services/assetApi';

const mockNavigate = vi.fn();

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
    auth: { token: 'test-token', user: { userId: 1, role: 'MANAGER' } },
    logout: vi.fn(),
  }),
}));

vi.mock('../../components/NotificationButton', () => ({
  default: () => <button type="button">Notifications</button>,
}));

function assetPageResponse(content) {
  return { content, number: 0, totalPages: 1 };
}

describe('BusinessTriggersPage', () => {
  afterEach(cleanup);

  beforeEach(() => {
    vi.clearAllMocks();
    businessTriggerApi.list.mockResolvedValue([]);
    assetApi.list.mockResolvedValue(assetPageResponse([]));
  });

  it('renders with mocked trigger and asset data', async () => {
    businessTriggerApi.list.mockResolvedValue([
      {
        id: 1,
        assetName: 'Central Playground',
        type: 'CUSTOMER_REQUEST',
        reason: 'Reported damage',
        urgent: false,
        createdAt: '2026-06-01T09:00:00',
      },
    ]);
    assetApi.list.mockResolvedValue(assetPageResponse([
      { id: 10, name: 'Central Playground', departmentName: 'Parks' },
    ]));

    render(
      <MemoryRouter>
        <BusinessTriggersPage />
      </MemoryRouter>
    );

    expect(await screen.findByText('Central Playground')).toBeInTheDocument();
    expect(screen.getByText('Reported damage')).toBeInTheDocument();
    expect(screen.getByRole('option', { name: 'Central Playground (Parks)' })).toBeInTheDocument();
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
});
