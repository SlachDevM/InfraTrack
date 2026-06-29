import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import AssignInspectionForm from '../../components/inspections/AssignInspectionForm';

describe('AssignInspectionForm', () => {
  it('renders assign controls', () => {
    render(
      <AssignInspectionForm
        formData={{
          businessTriggerId: '1',
          assignedToUserId: '20',
          priority: 'NORMAL',
          expectedCompletionDate: '2026-06-15',
        }}
        triggers={[
          {
            id: 1,
            assetName: 'Central Playground',
            type: 'CUSTOMER_REQUEST',
            reason: 'Damaged equipment',
            urgent: false,
          },
        ]}
        workers={[{ id: 20, name: 'Alex Field', role: 'FIELD_EMPLOYEE' }]}
        selectedTrigger={{
          id: 1,
          assetName: 'Central Playground',
          type: 'CUSTOMER_REQUEST',
          reason: 'Damaged equipment',
          urgent: false,
        }}
        publishedTemplates={[]}
        submitting={false}
        onChange={vi.fn()}
        onSubmit={vi.fn((event) => event.preventDefault())}
      />
    );

    expect(screen.getByLabelText('Business Trigger')).toBeInTheDocument();
    expect(screen.getByLabelText('Assign To')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Assign Inspection' })).toBeInTheDocument();
  });
});
