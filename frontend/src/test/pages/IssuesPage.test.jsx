import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, waitFor, cleanup } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import IssuesPage from '../../pages/IssuesPage';
import issueApi from '../../services/issueApi';
import inspectionApi from '../../services/inspectionApi';

const mockNavigate = vi.fn();

vi.mock('../../services/issueApi', () => ({
  default: {
    list: vi.fn(),
    record: vi.fn(),
  },
}));

vi.mock('../../services/inspectionApi', () => ({
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

function inspectionPageResponse(content) {
  return { content, number: 0, totalPages: 1 };
}

describe('IssuesPage', () => {
  afterEach(cleanup);

  beforeEach(() => {
    vi.clearAllMocks();
    issueApi.list.mockResolvedValue([]);
    inspectionApi.list.mockResolvedValue(inspectionPageResponse([]));
  });

  it('renders issues from mocked API', async () => {
    issueApi.list.mockResolvedValue([
      {
        id: 1,
        assetName: 'Central Playground',
        inspectionId: 10,
        description: 'Broken swing',
        severity: 'HIGH',
        recordedAt: '2026-06-01T09:00:00',
      },
    ]);
    inspectionApi.list.mockResolvedValue(inspectionPageResponse([
      {
        id: 10,
        assetName: 'Central Playground',
        status: 'COMPLETED',
        issueIdentified: true,
        completedByUserId: 1,
        businessTriggerType: 'CUSTOMER_REQUEST',
        observations: 'Swing chain broken',
        completedAt: '2026-06-01T08:00:00',
      },
    ]));

    render(
      <MemoryRouter>
        <IssuesPage />
      </MemoryRouter>
    );

    expect(await screen.findByText('Central Playground')).toBeInTheDocument();
    expect(screen.getByText('Broken swing')).toBeInTheDocument();
    expect(screen.getByText('#10')).toBeInTheDocument();
  });

  it('renders empty state without crashing when inspections return a Page object', async () => {
    render(
      <MemoryRouter>
        <IssuesPage />
      </MemoryRouter>
    );

    expect(await screen.findByText('No issues recorded yet.')).toBeInTheDocument();
  });

  it('displays API error message when loading fails', async () => {
    issueApi.list.mockRejectedValue({
      status: 500,
      message: 'Internal server error',
    });

    render(
      <MemoryRouter>
        <IssuesPage />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('Internal server error')).toBeInTheDocument();
    });
  });
});
