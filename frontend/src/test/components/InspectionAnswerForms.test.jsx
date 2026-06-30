import { describe, it, expect, vi, afterEach } from 'vitest';
import { render, screen, cleanup } from '@testing-library/react';
import AssignInspectionForm from '../../components/inspections/AssignInspectionForm';
import CompleteInspectionForm from '../../components/inspections/CompleteInspectionForm';

describe('AssignInspectionForm template selector', () => {
  afterEach(cleanup);

  it('shows published template selector when eligible templates exist', () => {
    render(
      <AssignInspectionForm
        formData={{
          businessTriggerId: '1',
          assignedToUserId: '20',
          inspectionTemplateId: '',
          priority: 'NORMAL',
          expectedCompletionDate: '',
        }}
        triggers={[
          {
            id: 1,
            assetName: 'Pump 1',
            type: 'CUSTOMER_REQUEST',
            reason: 'Routine check',
            urgent: false,
          },
        ]}
        workers={[{ id: 20, name: 'Alex Field' }]}
        selectedTrigger={{
          id: 1,
          assetName: 'Pump 1',
          type: 'CUSTOMER_REQUEST',
          reason: 'Routine check',
          urgent: false,
        }}
        publishedTemplates={[
          { id: 50, name: 'Pump Inspection', version: 1, status: 'PUBLISHED', assetCategoryId: 10 },
        ]}
        submitting={false}
        onChange={vi.fn()}
        onSubmit={vi.fn((event) => event.preventDefault())}
      />
    );

    expect(screen.getByLabelText('Inspection Template')).toBeInTheDocument();
    expect(screen.getByText('Pump Inspection (v1)')).toBeInTheDocument();
    expect(screen.getByText('No template (legacy free-text inspection)')).toBeInTheDocument();
  });
});

describe('CompleteInspectionForm checklist UI', () => {
  afterEach(cleanup);

  const inspection = {
    id: 100,
    assetName: 'Pump 1',
    businessTriggerId: 1,
    businessTriggerType: 'CUSTOMER_REQUEST',
    businessTriggerReason: 'Routine check',
    inspectionTemplateId: 50,
    inspectionTemplateName: 'Pump Inspection',
  };

  it('renders supported checklist questions for templated inspection', () => {
    render(
      <CompleteInspectionForm
        inspection={inspection}
        completeFormData={{
          observedCondition: 'GOOD',
          observations: '',
          issueIdentified: false,
          completedAt: '2026-06-15T10:00',
        }}
        templateQuestions={[
          {
            id: 1,
            code: 'LEAK',
            questionText: 'Is there any visible leak?',
            questionType: 'BOOLEAN',
            required: true,
            active: true,
          },
          {
            id: 2,
            code: 'NOTES',
            questionText: 'Additional notes',
            questionType: 'TEXT',
            required: false,
            active: true,
          },
        ]}
        answerValues={{}}
        completingId={null}
        onChange={vi.fn()}
        onAnswerChange={vi.fn()}
        onSubmit={vi.fn((event) => event.preventDefault())}
      />
    );

    expect(screen.getByText(/Is there any visible leak/i)).toBeInTheDocument();
    expect(screen.getByText(/Additional notes/i)).toBeInTheDocument();
    expect(screen.queryByText(/No active checklist questions are defined/i)).not.toBeInTheDocument();
    expect(screen.getByLabelText(/Observed Condition/i)).toBeInTheDocument();
  });

  it('shows unsupported placeholder for photo questions', () => {
    render(
      <CompleteInspectionForm
        inspection={inspection}
        completeFormData={{
          observedCondition: 'GOOD',
          observations: '',
          issueIdentified: false,
          completedAt: '2026-06-15T10:00',
        }}
        templateQuestions={[
          {
            id: 3,
            code: 'PHOTO_EVIDENCE',
            questionText: 'Attach photo evidence',
            questionType: 'PHOTO',
            required: false,
            active: true,
          },
        ]}
        answerValues={{}}
        completingId={null}
        onChange={vi.fn()}
        onAnswerChange={vi.fn()}
        onSubmit={vi.fn((event) => event.preventDefault())}
      />
    );

    expect(screen.getByText(/future sprint/i)).toBeInTheDocument();
  });

  it('renders choice dropdown and number unit for value model questions', () => {
    render(
      <CompleteInspectionForm
        inspection={inspection}
        completeFormData={{
          observedCondition: 'GOOD',
          observations: '',
          issueIdentified: false,
          completedAt: '2026-06-15T10:00',
        }}
        templateQuestions={[
          {
            id: 4,
            code: 'CONDITION',
            questionText: 'Overall condition',
            questionType: 'CHOICE',
            required: true,
            active: true,
            choices: [
              { id: 10, code: 'GOOD', label: 'Good', displayOrder: 1, active: true },
              { id: 11, code: 'FAIR', label: 'Fair', displayOrder: 2, active: true },
            ],
          },
          {
            id: 5,
            code: 'TEMPERATURE',
            questionText: 'Temperature',
            questionType: 'NUMBER',
            required: true,
            active: true,
            unit: '°C',
            unitSymbol: '°C',
            unitName: 'Celsius',
            minValue: 0,
            maxValue: 120,
            decimalPlaces: 1,
          },
        ]}
        answerValues={{}}
        completingId={null}
        onChange={vi.fn()}
        onAnswerChange={vi.fn()}
        onSubmit={vi.fn((event) => event.preventDefault())}
      />
    );

    expect(screen.getByLabelText(/Overall condition/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Temperature/i)).toBeInTheDocument();
    expect(screen.getByText('°C')).toBeInTheDocument();
    expect(screen.getByText(/Maximum decimal places: 1/i)).toBeInTheDocument();
  });

  it('keeps legacy completion fields for non-templated inspection', () => {
    render(
      <CompleteInspectionForm
        inspection={{
          ...inspection,
          inspectionTemplateId: null,
          inspectionTemplateName: null,
        }}
        completeFormData={{
          observedCondition: 'GOOD',
          observations: '',
          issueIdentified: false,
          completedAt: '2026-06-15T10:00',
        }}
        templateQuestions={[]}
        answerValues={{}}
        completingId={null}
        onChange={vi.fn()}
        onAnswerChange={vi.fn()}
        onSubmit={vi.fn((event) => event.preventDefault())}
      />
    );

    expect(screen.queryAllByText(/Checklist Questions/i)).toHaveLength(0);
    expect(screen.getByLabelText(/Observations/i)).toBeInTheDocument();
  });
});
