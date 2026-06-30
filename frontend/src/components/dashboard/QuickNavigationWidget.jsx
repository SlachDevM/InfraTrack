export default function QuickNavigationWidget({ links, onNavigate }) {
  if (!links?.length) {
    return null;
  }

  return (
    <section className="dashboard-section" aria-label="Quick navigation">
      <h2>Quick navigation</h2>
      <div className="dashboard-quick-links">
        {links.map((link) => (
          <button
            key={link.path}
            type="button"
            className="dashboard-quick-link"
            onClick={() => onNavigate(link.path)}
          >
            {link.label}
          </button>
        ))}
      </div>
    </section>
  );
}
