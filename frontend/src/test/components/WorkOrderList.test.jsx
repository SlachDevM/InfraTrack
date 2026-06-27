import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import WorkOrderList from '../../components/workorders/WorkOrderList';

describe('WorkOrderList', () => {
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

    expect(screen.getByText('ASSIGNED')).toBeInTheDocument();
    expect(screen.getByText('Alex Field')).toBeInTheDocument();
    expect(screen.getByText('Central Playground')).toBeInTheDocument();
  });
});
