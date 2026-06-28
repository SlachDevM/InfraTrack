import { describe, it, expect, vi, afterEach } from 'vitest';
import { render, screen, cleanup } from '@testing-library/react';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import PrivateRoute from '../components/PrivateRoute';
import DepartmentsPage from '../pages/DepartmentsPage';
import { USER_ROLES } from '../constants/userRoles';

const mockLogout = vi.fn();

const { mockAuth, mockLoading } = vi.hoisted(() => ({
  mockAuth: {
    token: 'test-token',
    user: { userId: 10, email: 'field@test.com', role: 'FIELD_EMPLOYEE' },
  },
  mockLoading: { current: false },
}));

vi.mock('../context/AuthContext', () => ({
  useAuth: () => ({
    auth: mockAuth,
    loading: mockLoading.current,
    logout: mockLogout,
  }),
}));

vi.mock('../services/apiClient', () => ({
  default: { setToken: vi.fn() },
}));

vi.mock('../services/departmentApi', () => ({
  default: { list: vi.fn().mockResolvedValue([]) },
}));

vi.mock('../components/NotificationButton', () => ({
  default: () => <button type="button">Notifications</button>,
}));

function renderProtectedRoute(initialEntry, page) {
  render(
    <MemoryRouter initialEntries={[initialEntry]}>
      <Routes>
        <Route
          path={initialEntry}
          element={
            <PrivateRoute>
              {page}
            </PrivateRoute>
          }
        />
      </Routes>
    </MemoryRouter>
  );
}

describe('PrivateRoute field employee protection', () => {
  afterEach(cleanup);

  it('shows forbidden page for hidden field employee routes', async () => {
    mockAuth.user.role = USER_ROLES.FIELD_EMPLOYEE;
    mockLoading.current = false;

    renderProtectedRoute('/departments', <DepartmentsPage />);

    expect(await screen.findByText('Access Denied')).toBeInTheDocument();
    expect(screen.getByText('You do not have permission to view this page.')).toBeInTheDocument();
  });

  it('allows managers to access administration routes', async () => {
    mockAuth.user.role = USER_ROLES.MANAGER;
    mockLoading.current = false;

    renderProtectedRoute('/departments', <DepartmentsPage />);

    expect(await screen.findByRole('heading', { name: 'Departments' })).toBeInTheDocument();
  });
});
