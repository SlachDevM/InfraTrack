import { COMMON_MESSAGES } from '../../constants/messages';

export default function AttentionRequiredWidget({ alerts }) {
  if (!alerts?.length) {
    return (
      <section className="dashboard-section" aria-label="Attention required">
        <h2>Attention required</h2>
        <div className="dashboard-state-message empty-state" role="status">
          {COMMON_MESSAGES.NO_ATTENTION_ITEMS}
        </div>
      </section>
    );
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
