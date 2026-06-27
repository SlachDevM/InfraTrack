import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, waitFor, cleanup } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import DelegatedAuthoritiesPage from '../../pages/DelegatedAuthoritiesPage';
import delegatedAuthorityApi from '../../services/delegatedAuthorityApi';
import departmentApi from '../../services/departmentApi';
import userApi from '../../services/userApi';

const mockNavigate = vi.fn();

const { mockAuth, mockLogout } = vi.hoisted(() => ({
  mockAuth: { token: 'test-token', user: { userId: 1, role: 'MANAGER' } },
  mockLogout: vi.fn(),
}));

vi.mock('../../services/delegatedAuthorityApi', () => ({
  default: {
    list: vi.fn(),
    create: vi.fn(),
    revoke: vi.fn(),
  },
}));

vi.mock('../../services/departmentApi', () => ({
  default: {
    list: vi.fn(),
  },
}));

vi.mock('../../services/userApi', () => ({
  default: {
    getManagers: vi.fn(),
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

describe('DelegatedAuthoritiesPage', () => {
  afterEach(cleanup);

  beforeEach(() => {
    vi.clearAllMocks();
    delegatedAuthorityApi.list.mockResolvedValue(pageResponse([]));
    departmentApi.list.mockResolvedValue([]);
    userApi.getManagers.mockResolvedValue([]);
  });

  it('renders authorities from mocked API', async () => {
    delegatedAuthorityApi.list.mockResolvedValue(pageResponse([
      {
        id: 1,
        delegatingManagerName: 'Alice Manager',
        delegateManagerName: 'Bob Manager',
        sourceDepartmentName: 'Parks',
        targetDepartmentName: 'Roads',
        reason: 'Annual leave cover',
        validFrom: '2026-06-01T09:00:00',
        validUntil: '2026-06-08T09:00:00',
        revoked: false,
      },
    ]));

    render(
      <MemoryRouter>
        <DelegatedAuthoritiesPage />
      </MemoryRouter>
    );

    expect(await screen.findByText('Annual leave cover')).toBeInTheDocument();
    expect(screen.getByText('Parks')).toBeInTheDocument();
  });

  it('loads the next page when Next is clicked', async () => {
    const user = userEvent.setup();
    delegatedAuthorityApi.list.mockImplementation((page = 0) => {
      if (page === 0) {
        return Promise.resolve(pageResponse([
          {
            id: 1,
            delegatingManagerName: 'Alice Manager',
            delegateManagerName: 'Bob Manager',
            sourceDepartmentName: 'Parks',
            targetDepartmentName: 'Roads',
            reason: 'First page delegation',
            validFrom: '2026-06-01T09:00:00',
            validUntil: '2026-06-08T09:00:00',
            revoked: false,
          },
        ], 0, 2));
      }
      return Promise.resolve(pageResponse([
        {
          id: 2,
          delegatingManagerName: 'Carol Manager',
          delegateManagerName: 'Dan Manager',
          sourceDepartmentName: 'Water',
          targetDepartmentName: 'Parks',
          reason: 'Second page delegation',
          validFrom: '2026-06-02T09:00:00',
          validUntil: '2026-06-09T09:00:00',
          revoked: false,
        },
      ], 1, 2));
    });

    render(
      <MemoryRouter>
        <DelegatedAuthoritiesPage />
      </MemoryRouter>
    );

    expect(await screen.findByText('First page delegation')).toBeInTheDocument();
    await user.click(screen.getByTestId('pagination-next'));

    await waitFor(() => {
      expect(delegatedAuthorityApi.list).toHaveBeenLastCalledWith(1);
      expect(screen.getByText('Second page delegation')).toBeInTheDocument();
    });
  });
});
