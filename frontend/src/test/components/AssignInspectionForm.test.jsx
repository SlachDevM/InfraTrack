import { describe, it, expect, vi, afterEach } from 'vitest';
import { render, screen, cleanup } from '@testing-library/react';
import AssignInspectionForm from '../../components/inspections/AssignInspectionForm';

const baseProps = {
  formData: {
    businessTriggerId: '1',
    assignedToUserId: '20',
    inspectionTemplateId: '',
    priority: 'NORMAL',
    expectedCompletionDate: '2026-06-15',
  },
  triggers: [
    {
      id: 1,
      assetName: 'Central Playground',
      type: 'CUSTOMER_REQUEST',
      reason: 'Damaged equipment',
      urgent: false,
    },
  ],
  workers: [{ id: 20, name: 'Alex Field', role: 'FIELD_EMPLOYEE' }],
  selectedTrigger: {
    id: 1,
    assetName: 'Central Playground',
    type: 'CUSTOMER_REQUEST',
    reason: 'Damaged equipment',
    urgent: false,
  },
  submitting: false,
  onChange: vi.fn(),
  onSubmit: vi.fn((event) => event.preventDefault()),
};

function getFieldOrder(container) {
  const labels = Array.from(container.querySelectorAll('label')).map((label) => label.textContent);
  return labels;
}

describe('AssignInspectionForm', () => {
  afterEach(cleanup);

  it('renders assign controls', () => {
    render(<AssignInspectionForm {...baseProps} publishedTemplates={[]} />);

    expect(screen.getByLabelText('Business Trigger')).toBeInTheDocument();
    expect(screen.getByLabelText('Assign To')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Assign Inspection' })).toBeInTheDocument();
    expect(screen.queryByLabelText('Inspection Template')).not.toBeInTheDocument();
  });

  it('shows published template selector between priority and expected completion date', () => {
    const { container } = render(
      <AssignInspectionForm
        {...baseProps}
        publishedTemplates={[
          { id: 50, name: 'Street Light Inspection', version: 1, status: 'PUBLISHED' },
        ]}
      />
    );

    expect(screen.getByLabelText('Inspection Template')).toBeInTheDocument();
    expect(screen.getByLabelText('Inspection Template')).toHaveDisplayValue(
      'No template (legacy free-text inspection)'
    );
    expect(
      screen.getByRole('option', { name: 'Street Light Inspection (v1)' })
    ).toBeInTheDocument();

    const labels = getFieldOrder(container);
    const priorityIndex = labels.indexOf('Priority');
    const templateIndex = labels.indexOf('Inspection Template');
    const expectedDateIndex = labels.indexOf('Expected Completion Date');
    expect(priorityIndex).toBeGreaterThan(-1);
    expect(templateIndex).toBeGreaterThan(priorityIndex);
    expect(expectedDateIndex).toBeGreaterThan(templateIndex);
  });

  it('supports legacy assignment without selecting a template', () => {
    render(
      <AssignInspectionForm
        {...baseProps}
        publishedTemplates={[
          { id: 50, name: 'Street Light Inspection', version: 1, status: 'PUBLISHED' },
        ]}
      />
    );

    expect(screen.getByLabelText('Inspection Template')).toContainHTML(
      'No template (legacy free-text inspection)'
    );
  });
});
