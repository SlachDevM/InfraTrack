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
import PreventiveMaintenancePlansPage from './pages/PreventiveMaintenancePlansPage';
import PreventiveExecutionCandidatesPage from './pages/PreventiveExecutionCandidatesPage';
import PreventiveSchedulerPage from './pages/PreventiveSchedulerPage';
import DashboardPage from './pages/DashboardPage';
import { ROUTES } from './constants/routes';
import './App.css';

function App() {
  return (
    <Router>
      <AuthProvider>
        <NotificationProvider>
          <Routes>
            <Route path={ROUTES.LOGIN} element={<Login />} />
            <Route path={ROUTES.ACTIVATE} element={<ActivationPage />} />
            <Route
              path={ROUTES.HOME}
              element={
                <PrivateRoute>
                  <PlatformShell />
                </PrivateRoute>
              }
            />
            <Route
              path={ROUTES.DASHBOARD}
              element={
                <PrivateRoute>
                  <DashboardPage />
                </PrivateRoute>
              }
            />
            <Route
              path={ROUTES.NOTIFICATIONS}
              element={
                <PrivateRoute>
                  <NotificationPage />
                </PrivateRoute>
              }
            />
            <Route
              path={ROUTES.USERS}
              element={
                <PrivateRoute>
                  <UserManagementPage />
                </PrivateRoute>
              }
            />
            <Route
              path={ROUTES.DEPARTMENTS}
              element={
                <PrivateRoute>
                  <DepartmentsPage />
                </PrivateRoute>
              }
            />
            <Route
              path={ROUTES.ASSET_CATEGORIES}
              element={
                <PrivateRoute>
                  <AssetCategoriesPage />
                </PrivateRoute>
              }
            />
            <Route
              path={ROUTES.ASSETS}
              element={
                <PrivateRoute>
                  <AssetsPage />
                </PrivateRoute>
              }
            />
            <Route
              path={ROUTES.BUSINESS_TRIGGERS}
              element={
                <PrivateRoute>
                  <BusinessTriggersPage />
                </PrivateRoute>
              }
            />
            <Route
              path={ROUTES.INSPECTIONS}
              element={
                <PrivateRoute>
                  <InspectionsPage />
                </PrivateRoute>
              }
            />
            <Route
              path={ROUTES.INSPECTION_TEMPLATES}
              element={
                <PrivateRoute>
                  <InspectionTemplatesPage />
                </PrivateRoute>
              }
            />
            <Route
              path={`${ROUTES.INSPECTION_TEMPLATES}/:templateId/questions`}
              element={
                <PrivateRoute>
                  <InspectionTemplateQuestionsPage />
                </PrivateRoute>
              }
            />
            <Route
              path={ROUTES.PREVENTIVE_PLANS}
              element={
                <PrivateRoute>
                  <PreventiveMaintenancePlansPage />
                </PrivateRoute>
              }
            />
            <Route
              path={ROUTES.PREVENTIVE_CANDIDATES}
              element={
                <PrivateRoute>
                  <PreventiveExecutionCandidatesPage />
                </PrivateRoute>
              }
            />
            <Route
              path={ROUTES.PREVENTIVE_SCHEDULER}
              element={
                <PrivateRoute>
                  <PreventiveSchedulerPage />
                </PrivateRoute>
              }
            />
            <Route
              path={ROUTES.ISSUES}
              element={
                <PrivateRoute>
                  <IssuesPage />
                </PrivateRoute>
              }
            />
            <Route
              path={ROUTES.OPERATIONAL_DECISIONS}
              element={
                <PrivateRoute>
                  <OperationalDecisionsPage />
                </PrivateRoute>
              }
            />
            <Route
              path={ROUTES.DELEGATED_AUTHORITIES}
              element={
                <PrivateRoute>
                  <DelegatedAuthoritiesPage />
                </PrivateRoute>
              }
            />
            <Route
              path={ROUTES.WORK_ORDERS}
              element={
                <PrivateRoute>
                  <WorkOrdersPage />
                </PrivateRoute>
              }
            />
            <Route path="*" element={<Navigate to={ROUTES.HOME} />} />
          </Routes>
        </NotificationProvider>
      </AuthProvider>
    </Router>
  );
}

export default App;
