import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import AssignWorkOrderForm from '../../components/workorders/AssignWorkOrderForm';

describe('AssignWorkOrderForm', () => {
  it('renders assignees and calls onSubmit', async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn((event) => event.preventDefault());

    render(
      <AssignWorkOrderForm
        assignFormData={{
          workOrderId: '100',
          assignedToUserId: '20',
          assignedAt: '2026-06-01T10:00',
        }}
        createdWorkOrders={[
          {
            id: 100,
            assetName: 'Central Playground',
            workType: 'INTERNAL_MAINTENANCE',
            description: 'Replace chain',
          },
        ]}
        selectedAssignWorkOrder={{
          id: 100,
          workType: 'INTERNAL_MAINTENANCE',
          description: 'Replace chain',
        }}
        eligibleAssignees={[{ id: 20, name: 'Alex Field', role: 'FIELD_EMPLOYEE' }]}
        assigning={false}
        onChange={vi.fn()}
        onSubmit={onSubmit}
      />
    );

    expect(screen.getByLabelText('Assign To')).toBeInTheDocument();
    expect(screen.getByText('Alex Field (FIELD_EMPLOYEE)')).toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: 'Assign Work Order' }));
    expect(onSubmit).toHaveBeenCalledTimes(1);
  });
});
