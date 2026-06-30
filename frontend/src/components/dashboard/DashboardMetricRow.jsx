export default function DashboardMetricRow({ label, value }) {
  return (
    <div className="dashboard-metric">
      <span>{label}</span>
      <strong>{value ?? 0}</strong>
    </div>
  );
}
