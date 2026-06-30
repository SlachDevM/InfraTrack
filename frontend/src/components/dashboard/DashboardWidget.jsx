export default function DashboardWidget({
  title,
  ariaLabel,
  loading = false,
  loadingMessage,
  error = null,
  empty = false,
  emptyMessage,
  children,
}) {
  return (
    <section className="dashboard-section" aria-label={ariaLabel}>
      {title && <h2>{title}</h2>}
      {loading && loadingMessage && (
        <div className="dashboard-state-message">{loadingMessage}</div>
      )}
      {!loading && error && (
        <div className="dashboard-state-message error">{error}</div>
      )}
      {!loading && !error && empty && emptyMessage && (
        <div className="dashboard-state-message">{emptyMessage}</div>
      )}
      {!loading && !error && !empty && children}
    </section>
  );
}
