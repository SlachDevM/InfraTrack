import { describe, it, expect, afterEach } from 'vitest';
import { render, screen, cleanup } from '@testing-library/react';
import { MemoryRouter, Routes, Route, Navigate } from 'react-router-dom';
import ActivationPage from '../pages/ActivationPage';

function TestRoutes({ auth }) {
  function PrivateRoute({ children }) {
    return auth ? children : <Navigate to="/login" />;
  }

  return (
    <Routes>
      <Route path="/login" element={<div>Login Page</div>} />
      <Route path="/activate" element={<ActivationPage />} />
      <Route
        path="/"
        element={
          <PrivateRoute>
            <div>Dashboard</div>
          </PrivateRoute>
        }
      />
      <Route path="*" element={<Navigate to="/" />} />
    </Routes>
  );
}

describe('Public activation route', () => {
  afterEach(cleanup);

  it('does not redirect /activate to login when unauthenticated', () => {
    render(
      <MemoryRouter initialEntries={['/activate?token=abc']}>
        <TestRoutes auth={null} />
      </MemoryRouter>
    );

    expect(screen.getByLabelText('Password')).toBeInTheDocument();
    expect(screen.queryByText('Login Page')).not.toBeInTheDocument();
  });
});
