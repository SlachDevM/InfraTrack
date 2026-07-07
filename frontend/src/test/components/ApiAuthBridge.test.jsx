import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, waitFor, cleanup } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import ApiAuthBridge from '../../components/ApiAuthBridge';
import apiClient from '../../services/apiClient';
import { notifyUnauthorized, registerUnauthorizedHandler } from '../../services/unauthorizedHandler';
import { ROUTES } from '../../constants/routes';

const mockLogout = vi.fn();
const mockNavigate = vi.fn();
let mockAuth = { token: 'jwt-token', user: { userId: 1, role: 'ADMINISTRATOR' } };
let mockLoading = false;

vi.mock('../../services/apiClient', () => ({
  default: {
    setToken: vi.fn(),
  },
}));

vi.mock('../../context/AuthContext', () => ({
  useAuth: () => ({
    auth: mockAuth,
    logout: mockLogout,
    loading: mockLoading,
  }),
}));

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

describe('ApiAuthBridge', () => {
  afterEach(cleanup);

  beforeEach(() => {
    vi.clearAllMocks();
    mockAuth = { token: 'jwt-token', user: { userId: 1, role: 'ADMINISTRATOR' } };
    mockLoading = false;
    registerUnauthorizedHandler(null);
  });

  it('registers unauthorized handler that clears session and redirects to login', async () => {
    render(
      <MemoryRouter>
        <ApiAuthBridge />
      </MemoryRouter>
    );

    notifyUnauthorized();

    await waitFor(() => {
      expect(mockLogout).toHaveBeenCalledTimes(1);
      expect(apiClient.setToken).toHaveBeenCalledWith(null);
      expect(mockNavigate).toHaveBeenCalledWith(ROUTES.LOGIN, {
        state: { sessionExpired: true },
        replace: true,
      });
    });
  });

  it('sets api token when auth is available', async () => {
    render(
      <MemoryRouter>
        <ApiAuthBridge />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(apiClient.setToken).toHaveBeenCalledWith('jwt-token');
    });
  });
});
