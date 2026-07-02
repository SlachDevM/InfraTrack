import { describe, it, expect, afterEach } from 'vitest';
import { render, screen, cleanup } from '@testing-library/react';
import DashboardWidget from '../../components/dashboard/DashboardWidget';

describe('DashboardWidget', () => {
  afterEach(cleanup);

  it('renders loading state with shared classes', () => {
    render(
      <DashboardWidget
        title="KPIs"
        ariaLabel="Operational KPIs"
        loading
        loadingMessage="Loading operational KPIs..."
      >
        <p>Content</p>
      </DashboardWidget>
    );

    const message = screen.getByText('Loading operational KPIs...');
    expect(message).toHaveClass('loading-state-inline');
    expect(message).toHaveAttribute('role', 'status');
    expect(screen.queryByText('Content')).not.toBeInTheDocument();
  });

  it('renders error state with alert role', () => {
    render(
      <DashboardWidget ariaLabel="Recent activity" error="Unable to load dashboard data.">
        <p>Content</p>
      </DashboardWidget>
    );

    const message = screen.getByRole('alert');
    expect(message).toHaveTextContent('Unable to load dashboard data.');
    expect(message).toHaveClass('error-state');
  });

  it('renders empty state consistently', () => {
    render(
      <DashboardWidget ariaLabel="Recent activity" empty emptyMessage="No recent activity yet.">
        <p>Content</p>
      </DashboardWidget>
    );

    expect(screen.getByText('No recent activity yet.')).toHaveClass('empty-state');
  });
});
