import DashboardMetricRow from './DashboardMetricRow';

export default function KpiOverviewWidget({ kpis }) {
  if (!kpis) {
    return null;
  }

  return (
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

      <section className="dashboard-section" aria-label="KPI details">
        <h2>Operational KPIs</h2>
        <div className="dashboard-kpi-grid">
          <article className="dashboard-card">
            <h3>Assets</h3>
            <DashboardMetricRow label="Total assets" value={kpis.assets?.totalAssets} />
            <DashboardMetricRow
              label="Without category"
              value={kpis.assets?.assetsWithoutCategory}
            />
            <DashboardMetricRow
              label="Without department"
              value={kpis.assets?.assetsWithoutDepartment}
            />
          </article>

          <article className="dashboard-card">
            <h3>Inspections</h3>
            <DashboardMetricRow label="Assigned" value={kpis.inspections?.assignedInspections} />
            <DashboardMetricRow label="Completed" value={kpis.inspections?.completedInspections} />
            <DashboardMetricRow label="Overdue" value={kpis.inspections?.overdueInspections} />
          </article>

          <article className="dashboard-card">
            <h3>Issues</h3>
            <DashboardMetricRow label="Open" value={kpis.issues?.openIssues} />
            <DashboardMetricRow label="Resolved" value={kpis.issues?.resolvedIssues} />
            <DashboardMetricRow label="Rework" value={kpis.issues?.reworkIssues} />
          </article>

          <article className="dashboard-card">
            <h3>Work Orders</h3>
            <DashboardMetricRow label="Open" value={kpis.workOrders?.openWorkOrders} />
            <DashboardMetricRow label="In progress" value={kpis.workOrders?.inProgressWorkOrders} />
            <DashboardMetricRow label="Completed" value={kpis.workOrders?.completedWorkOrders} />
          </article>

          <article className="dashboard-card">
            <h3>Preventive</h3>
            <DashboardMetricRow
              label="Active plans"
              value={kpis.preventive?.activePreventivePlans}
            />
            <DashboardMetricRow
              label="Pending candidates"
              value={kpis.preventive?.pendingExecutionCandidates}
            />
            <DashboardMetricRow
              label="Approved candidates"
              value={kpis.preventive?.approvedExecutionCandidates}
            />
            <DashboardMetricRow
              label="Scheduler runs today"
              value={kpis.preventive?.schedulerRunsToday}
            />
          </article>

          <article className="dashboard-card">
            <h3>Decision Engine</h3>
            <DashboardMetricRow
              label="Rule evaluation reports"
              value={kpis.decisionEngine?.ruleEvaluationReports}
            />
            <DashboardMetricRow
              label="Pending suggestions"
              value={kpis.decisionEngine?.suggestedActionsPending}
            />
            <DashboardMetricRow
              label="Accepted suggestions"
              value={kpis.decisionEngine?.suggestedActionsAccepted}
            />
          </article>
        </div>
      </section>

      <section className="dashboard-section" aria-label="Recent intelligence">
        <h2>Recent intelligence</h2>
        <div className="dashboard-intelligence-grid">
          <article className="dashboard-card">
            <DashboardMetricRow
              label="Rule evaluation reports"
              value={kpis.decisionEngine?.ruleEvaluationReports}
            />
          </article>
          <article className="dashboard-card">
            <DashboardMetricRow
              label="Pending suggestions"
              value={kpis.decisionEngine?.suggestedActionsPending}
            />
            <DashboardMetricRow
              label="Accepted suggestions"
              value={kpis.decisionEngine?.suggestedActionsAccepted}
            />
            <DashboardMetricRow
              label="Rejected suggestions"
              value={kpis.decisionEngine?.suggestedActionsRejected}
            />
            <DashboardMetricRow
              label="Dismissed suggestions"
              value={kpis.decisionEngine?.suggestedActionsDismissed}
            />
          </article>
          <article className="dashboard-card">
            <DashboardMetricRow
              label="Scheduler runs today"
              value={kpis.preventive?.schedulerRunsToday}
            />
            <DashboardMetricRow
              label="Eligible plans now"
              value={kpis.preventive?.eligiblePlansNow}
            />
          </article>
        </div>
      </section>
    </>
  );
}
