import { describe, it, expect, vi } from 'vitest';
import { render, screen, within } from '@testing-library/react';
import InspectionList from '../../components/inspections/InspectionList';

const paginationProps = {
  page: 0,
  totalPages: 3,
  listLoading: false,
  onPrevious: vi.fn(),
  onNext: vi.fn(),
};

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
        {...paginationProps}
      />
    );

    expect(screen.getByText('Central Playground')).toBeInTheDocument();
    expect(screen.getByText('Assigned', { selector: 'span.status-badge' })).toBeInTheDocument();
    expect(screen.getByText('Alex Field')).toBeInTheDocument();
  });

  it('shows empty state when no inspections are listed', () => {
    render(<InspectionList inspections={[]} {...paginationProps} />);

    expect(screen.getByText('No inspections match the current filters.')).toBeInTheDocument();
  });

  it('renders pagination controls inside the inspection list section before rule evaluation reports', () => {
    const { container } = render(
      <InspectionList
        inspections={[
          {
            id: 60,
            assetName: 'Bridge A',
            businessTriggerId: 2,
            businessTriggerType: 'SCHEDULED_INSPECTION',
            assignedToUserName: 'Sam Field',
            assignedToUserId: 21,
            priority: 'HIGH',
            status: 'COMPLETED',
            observedCondition: 'GOOD',
            issueIdentified: false,
            expectedCompletionDate: '2026-06-10',
            completedAt: '2026-06-10T12:00:00',
            createdAt: '2026-06-01T09:00:00',
            inspectionTemplateId: 99,
          },
        ]}
        {...paginationProps}
      />
    );

    const listSection = container.querySelector('.inspection-list-section');
    expect(listSection).not.toBeNull();
    expect(within(listSection).getByTestId('pagination-next')).toBeInTheDocument();

    const pagination = listSection.querySelector('.pagination-controls');
    const ruleSection = listSection.querySelector('.rule-evaluation-reports-section');
    expect(pagination).not.toBeNull();
    expect(ruleSection).not.toBeNull();

    const orderedSections = [...listSection.querySelectorAll('.pagination-controls, .rule-evaluation-reports-section')];
    expect(orderedSections[0]).toBe(pagination);
    expect(orderedSections[1]).toBe(ruleSection);
  });
});
