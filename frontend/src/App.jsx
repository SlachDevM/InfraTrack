import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { NotificationProvider } from './context/NotificationContext';
import Login from './pages/Login';
import PlatformShell from './pages/PlatformShell';
import NotificationPage from './pages/NotificationPage';
import UserManagementPage from './pages/UserManagementPage';
import DepartmentsPage from './pages/DepartmentsPage';
import AssetCategoriesPage from './pages/AssetCategoriesPage';
import AssetsPage from './pages/AssetsPage';
import BusinessTriggersPage from './pages/BusinessTriggersPage';
import './App.css';

function PrivateRoute({ children }) {
  const { auth, loading } = useAuth();

  if (loading) {
    return <div className="loading">Loading...</div>;
  }

  return auth ? children : <Navigate to="/login" />;
}

function App() {
  return (
    <Router>
      <AuthProvider>
        <NotificationProvider>
          <Routes>
            <Route path="/login" element={<Login />} />
            <Route
              path="/"
              element={
                <PrivateRoute>
                  <PlatformShell />
                </PrivateRoute>
              }
            />
            <Route
              path="/notifications"
              element={
                <PrivateRoute>
                  <NotificationPage />
                </PrivateRoute>
              }
            />
            <Route
              path="/users"
              element={
                <PrivateRoute>
                  <UserManagementPage />
                </PrivateRoute>
              }
            />
            <Route
              path="/departments"
              element={
                <PrivateRoute>
                  <DepartmentsPage />
                </PrivateRoute>
              }
            />
            <Route
              path="/asset-categories"
              element={
                <PrivateRoute>
                  <AssetCategoriesPage />
                </PrivateRoute>
              }
            />
            <Route
              path="/assets"
              element={
                <PrivateRoute>
                  <AssetsPage />
                </PrivateRoute>
              }
            />
            <Route
              path="/business-triggers"
              element={
                <PrivateRoute>
                  <BusinessTriggersPage />
                </PrivateRoute>
              }
            />
            <Route path="*" element={<Navigate to="/" />} />
          </Routes>
        </NotificationProvider>
      </AuthProvider>
    </Router>
  );
}

export default App;
