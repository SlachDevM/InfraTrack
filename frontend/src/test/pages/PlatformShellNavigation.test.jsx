import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, cleanup } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import PlatformShell from '../../pages/PlatformShell';
import { USER_ROLES } from '../../constants/userRoles';

const mockNavigate = vi.fn();
const mockLogout = vi.fn();

const { mockAuth } = vi.hoisted(() => ({
  mockAuth: {
    token: 'test-token',
    user: { userId: 10, email: 'user@test.com', role: 'FIELD_EMPLOYEE' },
  },
}));

vi.mock('../../context/AuthContext', () => ({
  useAuth: () => ({
    auth: mockAuth,
    logout: mockLogout,
  }),
}));

vi.mock('../../components/NotificationButton', () => ({
  default: () => <button type="button">Notifications</button>,
}));

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

describe('PlatformShell navigation', () => {
  afterEach(cleanup);

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('shows only operational nav items for field employees', () => {
    mockAuth.user.role = USER_ROLES.FIELD_EMPLOYEE;

    render(
      <MemoryRouter>
        <PlatformShell />
      </MemoryRouter>
    );

    expect(screen.getByText('Documents')).toBeInTheDocument();
    expect(screen.getByText('Inspections')).toBeInTheDocument();
    expect(screen.getByText('Work Orders')).toBeInTheDocument();
    expect(screen.queryByText('Departments')).not.toBeInTheDocument();
    expect(screen.queryByText('Issues')).not.toBeInTheDocument();
    expect(screen.queryByText('Business Triggers')).not.toBeInTheDocument();
    expect(screen.getByText('Assigned Inspections')).toBeInTheDocument();
    expect(screen.getByText('Operational Documents')).toBeInTheDocument();
  });

  it('keeps full navigation for managers', () => {
    mockAuth.user.role = USER_ROLES.MANAGER;

    render(
      <MemoryRouter>
        <PlatformShell />
      </MemoryRouter>
    );

    expect(screen.getByText('Assets')).toBeInTheDocument();
    expect(screen.getByText('Departments')).toBeInTheDocument();
    expect(screen.getByText('Issues')).toBeInTheDocument();
    expect(screen.getByText('Decisions')).toBeInTheDocument();
    expect(screen.queryByText('Assigned Inspections')).not.toBeInTheDocument();
  });

  it('keeps full navigation for operational coordinators', () => {
    mockAuth.user.role = USER_ROLES.OPERATIONAL_COORDINATOR;

    render(
      <MemoryRouter>
        <PlatformShell />
      </MemoryRouter>
    );

    expect(screen.getByText('Business Triggers')).toBeInTheDocument();
    expect(screen.getByText('Delegations')).toBeInTheDocument();
    expect(screen.getByText('Categories')).toBeInTheDocument();
  });

  it('keeps user management link for administrators only', () => {
    mockAuth.user.role = USER_ROLES.ADMINISTRATOR;

    render(
      <MemoryRouter>
        <PlatformShell />
      </MemoryRouter>
    );

    expect(screen.getByText('Users')).toBeInTheDocument();
    expect(screen.getByText('Departments')).toBeInTheDocument();
  });
});
