import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, cleanup } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
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

    expect(screen.getByRole('button', { name: 'Documents' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Inspections' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Work Orders' })).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'More' })).not.toBeInTheDocument();
    expect(screen.queryByText('Departments')).not.toBeInTheDocument();
    expect(screen.queryByText('Business Triggers')).not.toBeInTheDocument();
    expect(screen.getByText('Assigned Inspections')).toBeInTheDocument();
    expect(screen.getByText('Operational Documents')).toBeInTheDocument();
  });

  it('keeps primary navigation visible for managers and moves configuration links to More', async () => {
    const user = userEvent.setup();
    mockAuth.user.role = USER_ROLES.MANAGER;

    render(
      <MemoryRouter>
        <PlatformShell />
      </MemoryRouter>
    );

    expect(screen.getByRole('button', { name: 'Assets' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Issues' })).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Departments' })).not.toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'More' })).toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: 'More' }));

    expect(screen.getByRole('menuitem', { name: 'Departments' })).toBeInTheDocument();
    expect(screen.getByRole('menuitem', { name: 'Inspection Templates' })).toBeInTheDocument();
    expect(screen.queryByText('Assigned Inspections')).not.toBeInTheDocument();
  });

  it('navigates when a More menu item is clicked', async () => {
    const user = userEvent.setup();
    mockAuth.user.role = USER_ROLES.MANAGER;

    render(
      <MemoryRouter>
        <PlatformShell />
      </MemoryRouter>
    );

    await user.click(screen.getByRole('button', { name: 'More' }));
    await user.click(screen.getByRole('menuitem', { name: 'Departments' }));

    expect(mockNavigate).toHaveBeenCalledWith('/departments');
  });

  it('keeps overflow navigation for operational coordinators in More', async () => {
    const user = userEvent.setup();
    mockAuth.user.role = USER_ROLES.OPERATIONAL_COORDINATOR;

    render(
      <MemoryRouter>
        <PlatformShell />
      </MemoryRouter>
    );

    expect(screen.getByRole('button', { name: 'More' })).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Business Triggers' })).not.toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: 'More' }));

    expect(screen.getByRole('menuitem', { name: 'Business Triggers' })).toBeInTheDocument();
    expect(screen.getByRole('menuitem', { name: 'Delegations' })).toBeInTheDocument();
    expect(screen.getByRole('menuitem', { name: 'Categories' })).toBeInTheDocument();
  });

  it('keeps user management in More for administrators only', async () => {
    const user = userEvent.setup();
    mockAuth.user.role = USER_ROLES.ADMINISTRATOR;

    render(
      <MemoryRouter>
        <PlatformShell />
      </MemoryRouter>
    );

    expect(screen.getByRole('button', { name: 'More' })).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Users' })).not.toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: 'More' }));

    expect(screen.getByRole('menuitem', { name: 'Users' })).toBeInTheDocument();
    expect(screen.getByRole('menuitem', { name: 'Departments' })).toBeInTheDocument();
  });
});
