import { describe, it, expect, afterEach } from 'vitest';
import { render, screen, within, cleanup } from '@testing-library/react';
import WorkOrderList from '../../components/workorders/WorkOrderList';

describe('WorkOrderList', () => {
  afterEach(() => {
    cleanup();
  });
  it('renders status and assigned user', () => {
    render(
      <WorkOrderList
        workOrders={[
          {
            id: 100,
            assetName: 'Central Playground',
            operationalDecisionId: 5,
            workType: 'INTERNAL_MAINTENANCE',
            description: 'Replace chain',
            priority: 'HIGH',
            status: 'ASSIGNED',
            assignedToUserName: 'Alex Field',
            createdAtBusinessDate: '2026-06-01T09:00:00',
            assignedAt: '2026-06-01T10:00:00',
          },
        ]}
        maintenanceActivities={[]}
      />
    );

    const table = screen.getByRole('table', { name: 'Work orders' });
    expect(
      within(table).getByText('Assigned', { selector: 'span.status-badge' })
    ).toBeInTheDocument();
    expect(screen.getByText('Alex Field')).toBeInTheDocument();
    expect(screen.getByText('Central Playground')).toBeInTheDocument();
  });

  it('shows unassigned when no assignee name is provided', () => {
    render(
      <WorkOrderList
        workOrders={[
          {
            id: 101,
            assetName: 'Pump Station',
            operationalDecisionId: 6,
            workType: 'INTERNAL_MAINTENANCE',
            description: 'Inspect pump',
            priority: 'NORMAL',
            status: 'CREATED',
            assignedToUserName: null,
            createdAtBusinessDate: '2026-06-01T09:00:00',
            assignedAt: null,
          },
        ]}
        maintenanceActivities={[]}
      />
    );

    const table = screen.getByRole('table', { name: 'Work orders' });
    expect(screen.getByText('Unassigned')).toBeInTheDocument();
    expect(
      within(table).getByText('Created', { selector: 'span.status-badge' })
    ).toBeInTheDocument();
  });

  it('shows empty state when no work orders are listed', () => {
    render(<WorkOrderList workOrders={[]} maintenanceActivities={[]} />);

    expect(screen.getByText('No work orders found.')).toBeInTheDocument();
  });
});
