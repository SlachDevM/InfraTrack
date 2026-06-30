export default function AttentionRequiredWidget({ alerts }) {
  if (!alerts?.length) {
    return null;
  }

  return (
    <section className="dashboard-section" aria-label="Attention required">
      <h2>Attention required</h2>
      <div className="dashboard-alert-list">
        {alerts.map((alert) => (
          <div key={alert.id} className="dashboard-alert-card">
            {alert.message}
          </div>
        ))}
      </div>
    </section>
  );
}
