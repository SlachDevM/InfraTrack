import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, waitFor, cleanup } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import OperationalDecisionsPage from '../../pages/OperationalDecisionsPage';
import operationalDecisionApi from '../../services/operationalDecisionApi';
import issueApi from '../../services/issueApi';
import { MAX_PAGE_SIZE } from '../../utils/pagination';

const mockNavigate = vi.fn();

const { mockAuth, mockLogout } = vi.hoisted(() => ({
  mockAuth: { token: 'test-token', user: { userId: 1, role: 'MANAGER' } },
  mockLogout: vi.fn(),
}));

vi.mock('../../services/operationalDecisionApi', () => ({
  default: {
    list: vi.fn(),
    create: vi.fn(),
  },
}));

vi.mock('../../services/issueApi', () => ({
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

describe('OperationalDecisionsPage', () => {
  afterEach(cleanup);

  beforeEach(() => {
    vi.clearAllMocks();
    operationalDecisionApi.list.mockResolvedValue(pageResponse([]));
    issueApi.list.mockResolvedValue(pageResponse([]));
  });

  it('renders decisions from mocked API', async () => {
    operationalDecisionApi.list.mockImplementation((page = 0, size = 20) => {
      if (size === MAX_PAGE_SIZE) {
        return Promise.resolve(pageResponse([]));
      }
      return Promise.resolve(pageResponse([
        {
          id: 1,
          assetName: 'Central Playground',
          issueId: 10,
          outcome: 'INTERNAL_MAINTENANCE',
          rationale: 'Replace swing chain',
          decidedAt: '2026-06-01T09:00:00',
        },
      ]));
    });

    render(
      <MemoryRouter>
        <OperationalDecisionsPage />
      </MemoryRouter>
    );

    expect(await screen.findByText('Central Playground')).toBeInTheDocument();
    expect(screen.getByText('Replace swing chain')).toBeInTheDocument();
  });

  it('loads the next page when Next is clicked', async () => {
    const user = userEvent.setup();
    operationalDecisionApi.list.mockImplementation((page = 0, size = 20) => {
      if (size === MAX_PAGE_SIZE) {
        return Promise.resolve(pageResponse([]));
      }
      if (page === 0) {
        return Promise.resolve(pageResponse([
          {
            id: 1,
            assetName: 'First Page Asset',
            issueId: 10,
            outcome: 'INTERNAL_MAINTENANCE',
            rationale: 'First page decision',
            decidedAt: '2026-06-01T09:00:00',
          },
        ], 0, 2));
      }
      return Promise.resolve(pageResponse([
        {
          id: 2,
          assetName: 'Second Page Asset',
          issueId: 11,
          outcome: 'CONTINUE_MONITORING',
          rationale: 'Second page decision',
          decidedAt: '2026-06-02T09:00:00',
        },
      ], 1, 2));
    });

    render(
      <MemoryRouter>
        <OperationalDecisionsPage />
      </MemoryRouter>
    );

    expect(await screen.findByText('First page decision')).toBeInTheDocument();
    await user.click(screen.getByTestId('pagination-next'));

    await waitFor(() => {
      expect(operationalDecisionApi.list).toHaveBeenLastCalledWith(1);
      expect(screen.getByText('Second page decision')).toBeInTheDocument();
    });
  });
});
