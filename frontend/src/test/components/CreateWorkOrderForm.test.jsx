import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import CreateWorkOrderForm from '../../components/workorders/CreateWorkOrderForm';

describe('CreateWorkOrderForm', () => {
  it('renders required fields and calls onSubmit', async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn((event) => event.preventDefault());

    render(
      <CreateWorkOrderForm
        formData={{
          operationalDecisionId: '1',
          description: 'Replace chain',
          priority: 'HIGH',
          createdAtBusinessDate: '2026-06-01T09:00',
        }}
        eligibleDecisions={[
          {
            id: 1,
            assetName: 'Central Playground',
            outcome: 'INTERNAL_MAINTENANCE',
            rationale: 'Damaged chain',
          },
        ]}
        selectedDecision={{
          id: 1,
          assetName: 'Central Playground',
          outcome: 'INTERNAL_MAINTENANCE',
          rationale: 'Damaged chain',
        }}
        submitting={false}
        onChange={vi.fn()}
        onSubmit={onSubmit}
      />
    );

    expect(screen.getByLabelText('Operational Decision')).toBeInTheDocument();
    expect(screen.getByLabelText('Description')).toBeInTheDocument();
    expect(screen.getByLabelText('Priority')).toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: 'Create Work Order' }));
    expect(onSubmit).toHaveBeenCalledTimes(1);
  });
});
