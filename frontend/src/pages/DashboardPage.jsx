import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import apiClient from '../services/apiClient';
import operationsIntelligenceApi from '../services/operationsIntelligenceApi';
import AppNavbar from '../components/navigation/AppNavbar';
import KpiOverviewWidget from '../components/dashboard/KpiOverviewWidget';
import AttentionRequiredWidget from '../components/dashboard/AttentionRequiredWidget';
import TrendWidget from '../components/dashboard/TrendWidget';
import QuickNavigationWidget from '../components/dashboard/QuickNavigationWidget';
import RecentActivityWidget from '../components/dashboard/RecentActivityWidget';
import { canViewOperationsDashboard } from '../constants/userRoles';
import { canAccessRoute } from '../constants/navigation';
import { getApiErrorMessage } from '../utils/apiError';
import { buildAttentionAlerts } from '../utils/operationsDashboardAlerts';
import { APP_VERSION } from '../config/appVersion';
import '../styles/PlatformShell.css';
import '../styles/DashboardPage.css';

const QUICK_NAVIGATION_LINKS = [
  { path: '/assets', label: 'Assets' },
  { path: '/inspections', label: 'Inspections' },
  { path: '/issues', label: 'Issues' },
  { path: '/work-orders', label: 'Work Orders' },
  { path: '/preventive-execution-candidates', label: 'Preventive Candidates' },
  { path: '/preventive-scheduler', label: 'Scheduler' },
];

function getDisplayName(user) {
  if (!user) {
    return 'there';
  }
  if (user.name?.trim()) {
    return user.name.trim();
  }
  if (user.email) {
    return user.email.split('@')[0];
  }
  return 'there';
}

export default function DashboardPage() {
  const navigate = useNavigate();
  const { auth, logout } = useAuth();
  const [kpis, setKpis] = useState(null);
  const [trends, setTrends] = useState(null);
  const [recentActivity, setRecentActivity] = useState(null);
  const [loading, setLoading] = useState(true);
  const [trendsLoading, setTrendsLoading] = useState(true);
  const [activityLoading, setActivityLoading] = useState(true);
  const [error, setError] = useState(null);
  const [trendsError, setTrendsError] = useState(null);
  const [activityError, setActivityError] = useState(null);

  const role = auth?.user?.role;

  useEffect(() => {
    if (!auth) {
      navigate('/login');
      return;
    }
    if (!canViewOperationsDashboard(role)) {
      navigate('/');
      return;
    }

    apiClient.setToken(auth.token);

    const loadKpis = async () => {
      try {
        setLoading(true);
        setError(null);
        const data = await operationsIntelligenceApi.getKpis();
        setKpis(data);
      } catch (err) {
        setError(getApiErrorMessage(err, 'Failed to load operational KPIs.'));
      } finally {
        setLoading(false);
      }
    };

    const loadTrends = async () => {
      try {
        setTrendsLoading(true);
        setTrendsError(null);
        const data = await operationsIntelligenceApi.getTrends({ bucket: 'DAY' });
        setTrends(data);
      } catch (err) {
        setTrendsError(getApiErrorMessage(err, 'Failed to load operational trends.'));
      } finally {
        setTrendsLoading(false);
      }
    };

    const loadRecentActivity = async () => {
      try {
        setActivityLoading(true);
        setActivityError(null);
        const data = await operationsIntelligenceApi.getRecentActivity({ limit: 20 });
        setRecentActivity(data);
      } catch (err) {
        setActivityError(getApiErrorMessage(err, 'Failed to load recent activity.'));
      } finally {
        setActivityLoading(false);
      }
    };

    loadKpis();
    loadTrends();
    loadRecentActivity();
  }, [auth, navigate, role]);

  const attentionAlerts = useMemo(() => buildAttentionAlerts(kpis), [kpis]);

  const quickLinks = useMemo(
    () => QUICK_NAVIGATION_LINKS.filter((link) => canAccessRoute(role, link.path)),
    [role],
  );

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  if (!auth || !canViewOperationsDashboard(role)) {
    return null;
  }

  return (
    <div className="dashboard-page">
      <AppNavbar onNavigate={navigate} onLogout={handleLogout} />

      <main className="dashboard-content">
        <header className="dashboard-greeting">
          <h1>
            Good morning,
            {' '}
            {getDisplayName(auth.user)}
          </h1>
          <p>Operational overview</p>
        </header>

        {loading && (
          <div className="dashboard-state-message">Loading operational KPIs...</div>
        )}

        {!loading && error && (
          <div className="dashboard-state-message error">{error}</div>
        )}

        {!loading && !error && kpis && (
          <>
            <KpiOverviewWidget kpis={kpis} />
            <AttentionRequiredWidget alerts={attentionAlerts} />
            <QuickNavigationWidget links={quickLinks} onNavigate={navigate} />
          </>
        )}

        <TrendWidget
          trends={trends}
          loading={trendsLoading}
          error={trendsError}
        />

        <RecentActivityWidget
          items={recentActivity?.items}
          loading={activityLoading}
          error={activityError}
          onNavigate={navigate}
        />
      </main>

      <footer className="platform-footer">
        InfraTrack v
        {APP_VERSION}
      </footer>
    </div>
  );
}
