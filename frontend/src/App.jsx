import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { NotificationProvider } from './context/NotificationContext';
import PrivateRoute from './components/PrivateRoute';
import Login from './pages/Login';
import ActivationPage from './pages/ActivationPage';
import PlatformShell from './pages/PlatformShell';
import NotificationPage from './pages/NotificationPage';
import UserManagementPage from './pages/UserManagementPage';
import DepartmentsPage from './pages/DepartmentsPage';
import AssetCategoriesPage from './pages/AssetCategoriesPage';
import AssetsPage from './pages/AssetsPage';
import BusinessTriggersPage from './pages/BusinessTriggersPage';
import InspectionsPage from './pages/InspectionsPage';
import IssuesPage from './pages/IssuesPage';
import OperationalDecisionsPage from './pages/OperationalDecisionsPage';
import DelegatedAuthoritiesPage from './pages/DelegatedAuthoritiesPage';
import WorkOrdersPage from './pages/WorkOrdersPage';
import InspectionTemplatesPage from './pages/InspectionTemplatesPage';
import InspectionTemplateQuestionsPage from './pages/InspectionTemplateQuestionsPage';
import './App.css';

function App() {
  return (
    <Router>
      <AuthProvider>
        <NotificationProvider>
          <Routes>
            <Route path="/login" element={<Login />} />
            <Route path="/activate" element={<ActivationPage />} />
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
            <Route
              path="/inspections"
              element={
                <PrivateRoute>
                  <InspectionsPage />
                </PrivateRoute>
              }
            />
            <Route
              path="/inspection-templates"
              element={
                <PrivateRoute>
                  <InspectionTemplatesPage />
                </PrivateRoute>
              }
            />
            <Route
              path="/inspection-templates/:templateId/questions"
              element={
                <PrivateRoute>
                  <InspectionTemplateQuestionsPage />
                </PrivateRoute>
              }
            />
            <Route
              path="/issues"
              element={
                <PrivateRoute>
                  <IssuesPage />
                </PrivateRoute>
              }
            />
            <Route
              path="/operational-decisions"
              element={
                <PrivateRoute>
                  <OperationalDecisionsPage />
                </PrivateRoute>
              }
            />
            <Route
              path="/delegated-authorities"
              element={
                <PrivateRoute>
                  <DelegatedAuthoritiesPage />
                </PrivateRoute>
              }
            />
            <Route
              path="/work-orders"
              element={
                <PrivateRoute>
                  <WorkOrdersPage />
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
