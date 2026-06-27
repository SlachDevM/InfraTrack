import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import InspectionList from '../../components/inspections/InspectionList';

describe('InspectionList', () => {
  it('renders inspections', () => {
    render(
      <InspectionList
        inspections={[
          {
            id: 50,
            assetName: 'Central Playground',
            businessTriggerId: 1,
            businessTriggerType: 'CUSTOMER_REQUEST',
            assignedToUserName: 'Alex Field',
            assignedToUserId: 20,
            priority: 'NORMAL',
            status: 'ASSIGNED',
            observedCondition: null,
            issueIdentified: false,
            expectedCompletionDate: '2026-06-15',
            completedAt: null,
            createdAt: '2026-06-01T09:00:00',
          },
        ]}
      />
    );

    expect(screen.getByText('Central Playground')).toBeInTheDocument();
    expect(screen.getByText('ASSIGNED')).toBeInTheDocument();
    expect(screen.getByText('Alex Field')).toBeInTheDocument();
  });
});
