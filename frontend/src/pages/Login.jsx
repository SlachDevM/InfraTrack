import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import apiClient from '../services/apiClient';
import { API_ENDPOINTS } from '../constants/apiEndpoints';
import { getApiErrorMessage } from '../utils/apiError';
import '../styles/Login.css';

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export default function Login() {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    const trimmedEmail = email.trim().toLowerCase();
    const trimmedPassword = password.trim();

    if (!trimmedEmail || !trimmedPassword || !EMAIL_PATTERN.test(trimmedEmail)) {
      setError('Please enter a valid email and password.');
      return;
    }

    try {
      const data = await apiClient.post(API_ENDPOINTS.AUTH_LOGIN, {
        email: trimmedEmail,
        password: trimmedPassword,
      });

      const { token, ...user } = data;
      login(user, token);
      navigate('/');
    } catch (err) {
      setError(getApiErrorMessage(err, 'Email or password is incorrect.'));
    }
  };

  return (
    <div className="login-container">
      <div className="login-card">
        <div className="login-header">
          <div className="logo-placeholder">
            <span className="logo-text">IT</span>
          </div>
          <h1>InfraTrack</h1>
        </div>

        {error && <div className="error-message">{error}</div>}

        <form onSubmit={handleSubmit} noValidate>
          <div className="form-group">
            <label>Email</label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              placeholder="you@example.com"
            />
          </div>

          <div className="form-group">
            <label>Password</label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              placeholder="••••••••"
            />
          </div>

          <button type="submit" className="submit-button">
            Login
          </button>
        </form>
      </div>
    </div>
  );
}
