import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, waitFor, cleanup } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import ActivationPage from '../../pages/ActivationPage';
import authApi from '../../services/authApi';

vi.mock('../../services/authApi', () => ({
  default: {
    activateAccount: vi.fn(),
  },
}));

describe('ActivationPage', () => {
  afterEach(cleanup);

  beforeEach(() => {
    vi.clearAllMocks();
  });

  function renderWithToken(token) {
    const path = token ? `/activate?token=${token}` : '/activate';
    return render(
      <MemoryRouter initialEntries={[path]}>
        <ActivationPage />
      </MemoryRouter>
    );
  }

  it('renders password form when token is present', () => {
    renderWithToken('valid-token');

    expect(screen.getByLabelText('Password')).toBeInTheDocument();
    expect(screen.getByLabelText('Confirm Password')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Activate Account' })).toBeInTheDocument();
  });

  it('shows a clear error when token is missing', () => {
    renderWithToken(null);

    expect(screen.getByText(/activation link is invalid or missing a token/i)).toBeInTheDocument();
    expect(screen.queryByLabelText('Password')).not.toBeInTheDocument();
  });

  it('prevents submit when password is shorter than twelve characters', async () => {
    const user = userEvent.setup();
    renderWithToken('valid-token');

    await user.type(screen.getByLabelText('Password'), 'short');
    await user.type(screen.getByLabelText('Confirm Password'), 'short');
    await user.click(screen.getByRole('button', { name: 'Activate Account' }));

    expect(screen.getByText(/must be between 12 and 128 characters/i)).toBeInTheDocument();
    expect(authApi.activateAccount).not.toHaveBeenCalled();
  });

  it('prevents submit when passwords do not match', async () => {
    const user = userEvent.setup();
    renderWithToken('valid-token');

    await user.type(screen.getByLabelText('Password'), 'password1234');
    await user.type(screen.getByLabelText('Confirm Password'), 'different');
    await user.click(screen.getByRole('button', { name: 'Activate Account' }));

    expect(screen.getByText('Passwords do not match.')).toBeInTheDocument();
    expect(authApi.activateAccount).not.toHaveBeenCalled();
  });

  it('calls auth API and shows success on activation', async () => {
    const user = userEvent.setup();
    authApi.activateAccount.mockResolvedValue({ token: 'jwt', userId: 1 });

    renderWithToken('valid-token');

    await user.type(screen.getByLabelText('Password'), 'password1234');
    await user.type(screen.getByLabelText('Confirm Password'), 'password1234');
    await user.click(screen.getByRole('button', { name: 'Activate Account' }));

    await waitFor(() => {
      expect(authApi.activateAccount).toHaveBeenCalledWith('valid-token', 'password1234');
      expect(screen.getByText(/account has been activated/i)).toBeInTheDocument();
      expect(screen.getByRole('link', { name: 'Go to Login' })).toHaveAttribute('href', '/login');
    });
  });

  it('displays backend errors via apiError', async () => {
    const user = userEvent.setup();
    authApi.activateAccount.mockRejectedValue({
      status: 410,
      message: 'Activation token has already been used',
    });

    renderWithToken('used-token');

    await user.type(screen.getByLabelText('Password'), 'password1234');
    await user.type(screen.getByLabelText('Confirm Password'), 'password1234');
    await user.click(screen.getByRole('button', { name: 'Activate Account' }));

    await waitFor(() => {
      expect(screen.getByText('Activation token has already been used')).toBeInTheDocument();
    });
  });
});
