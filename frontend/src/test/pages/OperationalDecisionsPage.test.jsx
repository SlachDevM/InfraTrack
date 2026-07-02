import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, waitFor, cleanup } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import OperationalDecisionsPage from '../../pages/OperationalDecisionsPage';
import operationalDecisionApi from '../../services/operationalDecisionApi';
import issueApi from '../../services/issueApi';
import { DEFAULT_PAGE, MAX_PAGE_SIZE } from '../../utils/pagination';

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
    listEligibleForOperationalDecision: vi.fn(),
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

const eligibleIssue = {
  id: 10,
  assetName: 'Central Playground',
  description: 'Broken swing chain',
  severity: 'HIGH',
  recordedAt: '2026-06-01T09:00:00',
};

describe('OperationalDecisionsPage', () => {
  afterEach(cleanup);

  beforeEach(() => {
    vi.clearAllMocks();
    operationalDecisionApi.list.mockResolvedValue(pageResponse([]));
    issueApi.listEligibleForOperationalDecision.mockResolvedValue(pageResponse([]));
  });

  it('renders decisions from mocked API', async () => {
    operationalDecisionApi.list.mockResolvedValue(
      pageResponse([
        {
          id: 1,
          assetName: 'Central Playground',
          issueId: 10,
          outcome: 'INTERNAL_MAINTENANCE',
          rationale: 'Replace swing chain',
          decidedAt: '2026-06-01T09:00:00',
        },
      ])
    );

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
    operationalDecisionApi.list.mockImplementation((page = 0) => {
      if (page === 0) {
        return Promise.resolve(
          pageResponse(
            [
              {
                id: 1,
                assetName: 'First Page Asset',
                issueId: 10,
                outcome: 'INTERNAL_MAINTENANCE',
                rationale: 'First page decision',
                decidedAt: '2026-06-01T09:00:00',
              },
            ],
            0,
            2
          )
        );
      }
      return Promise.resolve(
        pageResponse(
          [
            {
              id: 2,
              assetName: 'Second Page Asset',
              issueId: 11,
              outcome: 'CONTINUE_MONITORING',
              rationale: 'Second page decision',
              decidedAt: '2026-06-02T09:00:00',
            },
          ],
          1,
          2
        )
      );
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

  it('shows eligible issue in selector from backend response', async () => {
    issueApi.listEligibleForOperationalDecision.mockResolvedValue(pageResponse([eligibleIssue]));

    render(
      <MemoryRouter>
        <OperationalDecisionsPage />
      </MemoryRouter>
    );

    expect(
      await screen.findByRole('option', {
        name: '#10 — Central Playground (High)',
      })
    ).toBeInTheDocument();
    expect(issueApi.listEligibleForOperationalDecision).toHaveBeenCalledWith(
      DEFAULT_PAGE,
      MAX_PAGE_SIZE
    );
  });

  it('does not show cross-department issues when backend returns only eligible issues', async () => {
    issueApi.listEligibleForOperationalDecision.mockResolvedValue(pageResponse([eligibleIssue]));

    render(
      <MemoryRouter>
        <OperationalDecisionsPage />
      </MemoryRouter>
    );

    await screen.findByRole('option', {
      name: '#10 — Central Playground (High)',
    });

    expect(
      screen.queryByRole('option', { name: /Other Department Asset/i })
    ).not.toBeInTheDocument();
  });

  it('shows empty selector message when no eligible issues exist', async () => {
    render(
      <MemoryRouter>
        <OperationalDecisionsPage />
      </MemoryRouter>
    );

    expect(
      await screen.findByText(/no issues are currently awaiting an operational decision/i)
    ).toBeInTheDocument();
    expect(screen.getByRole('option', { name: 'Select issue' })).toBeInTheDocument();
  });

  it('records operational decision after selecting eligible issue', async () => {
    const user = userEvent.setup();
    issueApi.listEligibleForOperationalDecision.mockResolvedValue(pageResponse([eligibleIssue]));
    operationalDecisionApi.create.mockResolvedValue({ id: 1 });

    render(
      <MemoryRouter>
        <OperationalDecisionsPage />
      </MemoryRouter>
    );

    await user.selectOptions(await screen.findByLabelText('Issue'), '10');
    await user.type(screen.getByLabelText('Rationale'), 'Replace swing chain internally');
    await user.click(screen.getByRole('button', { name: 'Make Operational Decision' }));

    await waitFor(() => {
      expect(operationalDecisionApi.create).toHaveBeenCalledWith(
        expect.objectContaining({
          issueId: 10,
          rationale: 'Replace swing chain internally',
          outcome: 'CONTINUE_MONITORING',
        })
      );
    });
  });

  it('displays forbidden message when backend rejects unauthorized decision', async () => {
    const user = userEvent.setup();
    issueApi.listEligibleForOperationalDecision.mockResolvedValue(pageResponse([eligibleIssue]));
    operationalDecisionApi.create.mockRejectedValue({ status: 403 });

    render(
      <MemoryRouter>
        <OperationalDecisionsPage />
      </MemoryRouter>
    );

    await user.selectOptions(await screen.findByLabelText('Issue'), '10');
    await user.type(screen.getByLabelText('Rationale'), 'Forged attempt');
    await user.click(screen.getByRole('button', { name: 'Make Operational Decision' }));

    await waitFor(() => {
      expect(
        screen.getByText('You do not have permission to make operational decisions.')
      ).toBeInTheDocument();
    });
  });

  it('displays API error message when loading fails', async () => {
    operationalDecisionApi.list.mockRejectedValue({
      status: 500,
      message: 'Internal server error',
    });

    render(
      <MemoryRouter>
        <OperationalDecisionsPage />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('Internal server error')).toBeInTheDocument();
    });
  });
});
