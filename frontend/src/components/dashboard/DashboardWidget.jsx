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
        <div className="dashboard-state-message loading-state-inline" role="status">
          {loadingMessage}
        </div>
      )}
      {!loading && error && (
        <div className="dashboard-state-message error error-state" role="alert">
          {error}
        </div>
      )}
      {!loading && !error && empty && emptyMessage && (
        <div className="dashboard-state-message empty-state">{emptyMessage}</div>
      )}
      {!loading && !error && !empty && children}
    </section>
  );
}
