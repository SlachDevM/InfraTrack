import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, waitFor, cleanup } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import Login from '../../pages/Login';
import apiClient from '../../services/apiClient';

const mockLogin = vi.fn();
const mockNavigate = vi.fn();

vi.mock('../../services/apiClient', () => ({
  default: {
    post: vi.fn(),
  },
}));

vi.mock('../../context/AuthContext', () => ({
  useAuth: () => ({
    login: mockLogin,
  }),
}));

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

describe('Login page', () => {
  afterEach(cleanup);

  beforeEach(() => {
    vi.clearAllMocks();
  });

  function renderLogin() {
    return render(
      <MemoryRouter>
        <Login />
      </MemoryRouter>
    );
  }

  async function submitLogin(user) {
    await user.type(screen.getByPlaceholderText('you@example.com'), 'user@example.com');
    await user.type(screen.getByPlaceholderText('••••••••'), 'secret123');
    await user.click(screen.getByRole('button', { name: 'Login' }));
  }

  it('logs in successfully and navigates home', async () => {
    const user = userEvent.setup();
    apiClient.post.mockResolvedValue({
      token: 'jwt-token',
      userId: 1,
      email: 'user@example.com',
      role: 'ADMINISTRATOR',
    });

    renderLogin();
    await submitLogin(user);

    await waitFor(() => {
      expect(apiClient.post).toHaveBeenCalledWith('/api/auth/login', {
        email: 'user@example.com',
        password: 'secret123',
      });
      expect(mockLogin).toHaveBeenCalledWith(
        { userId: 1, email: 'user@example.com', role: 'ADMINISTRATOR' },
        'jwt-token'
      );
      expect(mockNavigate).toHaveBeenCalledWith('/dashboard');
    });
  });

  it('logs in field employees to home', async () => {
    const user = userEvent.setup();
    apiClient.post.mockResolvedValue({
      token: 'jwt-token',
      userId: 2,
      email: 'field@example.com',
      role: 'FIELD_EMPLOYEE',
    });

    renderLogin();
    await submitLogin(user);

    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith('/');
    });
  });

  it('displays friendly retry message when rate limited with retryAfterSeconds', async () => {
    const user = userEvent.setup();
    apiClient.post.mockRejectedValue({
      status: 429,
      message: JSON.stringify({
        message: 'Too many login attempts. Please try again later.',
        retryAfterSeconds: 60,
      }),
    });

    renderLogin();
    await submitLogin(user);

    await waitFor(() => {
      expect(
        screen.getByText('Too many login attempts. Please try again in 60 seconds.')
      ).toBeInTheDocument();
    });
  });

  it('displays generic backend error when retryAfterSeconds is absent', async () => {
    const user = userEvent.setup();
    apiClient.post.mockRejectedValue({
      status: 401,
      message: 'Email or password is incorrect.',
    });

    renderLogin();
    await submitLogin(user);

    await waitFor(() => {
      expect(screen.getByText('Email or password is incorrect.')).toBeInTheDocument();
    });
  });

  it('uses fallback message for failed login without backend detail', async () => {
    const user = userEvent.setup();
    apiClient.post.mockRejectedValue({ status: 401 });

    renderLogin();
    await submitLogin(user);

    await waitFor(() => {
      expect(screen.getByText('Email or password is incorrect.')).toBeInTheDocument();
    });
  });
});
