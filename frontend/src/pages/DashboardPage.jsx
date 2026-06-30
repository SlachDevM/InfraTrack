import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import apiClient from '../services/apiClient';
import operationsIntelligenceApi from '../services/operationsIntelligenceApi';
import AppNavbar from '../components/navigation/AppNavbar';
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

function MetricRow({ label, value }) {
  return (
    <div className="dashboard-metric">
      <span>{label}</span>
      <strong>{value ?? 0}</strong>
    </div>
  );
}

export default function DashboardPage() {
  const navigate = useNavigate();
  const { auth, logout } = useAuth();
  const [kpis, setKpis] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

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

    loadKpis();
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
            <section className="dashboard-section" aria-label="Operational overview">
              <div className="dashboard-overview-grid">
                <article className="dashboard-card">
                  <div className="dashboard-overview-value">{kpis.assets?.totalAssets ?? 0}</div>
                  <div className="dashboard-overview-label">Assets</div>
                </article>
                <article className="dashboard-card">
                  <div className="dashboard-overview-value">{kpis.issues?.openIssues ?? 0}</div>
                  <div className="dashboard-overview-label">Open Issues</div>
                </article>
                <article className="dashboard-card">
                  <div className="dashboard-overview-value">{kpis.workOrders?.openWorkOrders ?? 0}</div>
                  <div className="dashboard-overview-label">Open Work Orders</div>
                </article>
                <article className="dashboard-card">
                  <div className="dashboard-overview-value">
                    {kpis.preventive?.pendingExecutionCandidates ?? 0}
                  </div>
                  <div className="dashboard-overview-label">Pending Preventive Candidates</div>
                </article>
              </div>
            </section>

            {attentionAlerts.length > 0 && (
              <section className="dashboard-section" aria-label="Attention required">
                <h2>Attention required</h2>
                <div className="dashboard-alert-list">
                  {attentionAlerts.map((alert) => (
                    <div key={alert.id} className="dashboard-alert-card">
                      {alert.message}
                    </div>
                  ))}
                </div>
              </section>
            )}

            <section className="dashboard-section" aria-label="KPI details">
              <h2>Operational KPIs</h2>
              <div className="dashboard-kpi-grid">
                <article className="dashboard-card">
                  <h3>Assets</h3>
                  <MetricRow label="Total assets" value={kpis.assets?.totalAssets} />
                  <MetricRow label="Without category" value={kpis.assets?.assetsWithoutCategory} />
                  <MetricRow label="Without department" value={kpis.assets?.assetsWithoutDepartment} />
                </article>

                <article className="dashboard-card">
                  <h3>Inspections</h3>
                  <MetricRow label="Assigned" value={kpis.inspections?.assignedInspections} />
                  <MetricRow label="Completed" value={kpis.inspections?.completedInspections} />
                  <MetricRow label="Overdue" value={kpis.inspections?.overdueInspections} />
                </article>

                <article className="dashboard-card">
                  <h3>Issues</h3>
                  <MetricRow label="Open" value={kpis.issues?.openIssues} />
                  <MetricRow label="Resolved" value={kpis.issues?.resolvedIssues} />
                  <MetricRow label="Rework" value={kpis.issues?.reworkIssues} />
                </article>

                <article className="dashboard-card">
                  <h3>Work Orders</h3>
                  <MetricRow label="Open" value={kpis.workOrders?.openWorkOrders} />
                  <MetricRow label="In progress" value={kpis.workOrders?.inProgressWorkOrders} />
                  <MetricRow label="Completed" value={kpis.workOrders?.completedWorkOrders} />
                </article>

                <article className="dashboard-card">
                  <h3>Preventive</h3>
                  <MetricRow label="Active plans" value={kpis.preventive?.activePreventivePlans} />
                  <MetricRow label="Pending candidates" value={kpis.preventive?.pendingExecutionCandidates} />
                  <MetricRow label="Approved candidates" value={kpis.preventive?.approvedExecutionCandidates} />
                  <MetricRow label="Scheduler runs today" value={kpis.preventive?.schedulerRunsToday} />
                </article>

                <article className="dashboard-card">
                  <h3>Decision Engine</h3>
                  <MetricRow label="Rule evaluation reports" value={kpis.decisionEngine?.ruleEvaluationReports} />
                  <MetricRow label="Pending suggestions" value={kpis.decisionEngine?.suggestedActionsPending} />
                  <MetricRow label="Accepted suggestions" value={kpis.decisionEngine?.suggestedActionsAccepted} />
                </article>
              </div>
            </section>

            <section className="dashboard-section" aria-label="Quick navigation">
              <h2>Quick navigation</h2>
              <div className="dashboard-quick-links">
                {quickLinks.map((link) => (
                  <button
                    key={link.path}
                    type="button"
                    className="dashboard-quick-link"
                    onClick={() => navigate(link.path)}
                  >
                    {link.label}
                  </button>
                ))}
              </div>
            </section>

            <section className="dashboard-section" aria-label="Recent intelligence">
              <h2>Recent intelligence</h2>
              <div className="dashboard-intelligence-grid">
                <article className="dashboard-card">
                  <MetricRow label="Rule evaluation reports" value={kpis.decisionEngine?.ruleEvaluationReports} />
                </article>
                <article className="dashboard-card">
                  <MetricRow label="Pending suggestions" value={kpis.decisionEngine?.suggestedActionsPending} />
                  <MetricRow label="Accepted suggestions" value={kpis.decisionEngine?.suggestedActionsAccepted} />
                  <MetricRow label="Rejected suggestions" value={kpis.decisionEngine?.suggestedActionsRejected} />
                  <MetricRow label="Dismissed suggestions" value={kpis.decisionEngine?.suggestedActionsDismissed} />
                </article>
                <article className="dashboard-card">
                  <MetricRow label="Scheduler runs today" value={kpis.preventive?.schedulerRunsToday} />
                  <MetricRow label="Eligible plans now" value={kpis.preventive?.eligiblePlansNow} />
                </article>
              </div>
            </section>
          </>
        )}
      </main>

      <footer className="platform-footer">
        InfraTrack v
        {APP_VERSION}
      </footer>
    </div>
  );
}
