import { useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import authApi from '../services/authApi';
import { ROUTES } from '../constants/routes';
import { getApiErrorMessage } from '../utils/apiError';
import {
  isPasswordLengthValid,
  PASSWORD_LENGTH_MESSAGE,
  PASSWORD_MAX_LENGTH,
  PASSWORD_MIN_LENGTH,
} from '../constants/passwordPolicy';
import '../styles/Login.css';

export default function ActivationPage() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token')?.trim() ?? '';

  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (!token) {
      setError('Activation link is invalid or missing a token.');
      return;
    }

    const trimmedPassword = password.trim();
    const trimmedConfirm = confirmPassword.trim();

    if (!trimmedPassword) {
      setError('Please enter a password.');
      return;
    }

    if (!isPasswordLengthValid(trimmedPassword)) {
      setError(PASSWORD_LENGTH_MESSAGE);
      return;
    }

    if (trimmedPassword !== trimmedConfirm) {
      setError('Passwords do not match.');
      return;
    }

    try {
      setSubmitting(true);
      await authApi.activateAccount(token, trimmedPassword);
      setSuccess(true);
    } catch (err) {
      setError(getApiErrorMessage(err, 'Account activation failed.'));
    } finally {
      setSubmitting(false);
    }
  };

  if (!token) {
    return (
      <div className="login-container">
        <div className="login-card">
          <div className="login-header">
            <div className="logo-placeholder">
              <span className="logo-text">IT</span>
            </div>
            <h1>Activate Account</h1>
          </div>
          <div className="error-message">
            This activation link is invalid or missing a token. Please contact an administrator to
            request a new invitation.
          </div>
          <p className="activation-footer">
            <Link to={ROUTES.LOGIN}>Back to Login</Link>
          </p>
        </div>
      </div>
    );
  }

  if (success) {
    return (
      <div className="login-container">
        <div className="login-card">
          <div className="login-header">
            <div className="logo-placeholder">
              <span className="logo-text">IT</span>
            </div>
            <h1>Account Activated</h1>
          </div>
          <div className="success-message">
            Your account has been activated. You can now log in with your new password.
          </div>
          <Link to={ROUTES.LOGIN} className="submit-button activation-login-link">
            Go to Login
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="login-container">
      <div className="login-card">
        <div className="login-header">
          <div className="logo-placeholder">
            <span className="logo-text">IT</span>
          </div>
          <h1>Activate Account</h1>
          <p className="activation-subtitle">Set your password to complete account setup.</p>
        </div>

        {error && <div className="error-message">{error}</div>}

        <form onSubmit={handleSubmit} noValidate>
          <div className="form-group">
            <label htmlFor="password">Password</label>
            <input
              id="password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              minLength={PASSWORD_MIN_LENGTH}
              maxLength={PASSWORD_MAX_LENGTH}
              disabled={submitting}
              placeholder="••••••••"
            />
          </div>

          <div className="form-group">
            <label htmlFor="confirmPassword">Confirm Password</label>
            <input
              id="confirmPassword"
              type="password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              required
              minLength={PASSWORD_MIN_LENGTH}
              maxLength={PASSWORD_MAX_LENGTH}
              disabled={submitting}
              placeholder="••••••••"
            />
          </div>

          <button type="submit" className="submit-button" disabled={submitting}>
            {submitting ? 'Activating...' : 'Activate Account'}
          </button>
        </form>

        <p className="activation-footer">
          <Link to={ROUTES.LOGIN}>Back to Login</Link>
        </p>
      </div>
    </div>
  );
}
