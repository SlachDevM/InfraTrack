import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import DecisionAssistantPanel from '../../components/inspections/DecisionAssistantPanel';
import suggestedActionApi from '../../services/suggestedActionApi';

const { mockAuth } = vi.hoisted(() => ({
  mockAuth: { token: 'test-token', user: { userId: 1, role: 'MANAGER' } },
}));

vi.mock('../../context/AuthContext', () => ({
  useAuth: () => ({ auth: mockAuth }),
}));

vi.mock('../../services/suggestedActionApi', () => ({
  default: {
    list: vi.fn(),
    getDetail: vi.fn(),
    approve: vi.fn(),
    reject: vi.fn(),
    dismiss: vi.fn(),
  },
}));

describe('DecisionAssistantPanel', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockAuth.user.role = 'MANAGER';
  });

  it('displays confidence badge and why panel for managers', async () => {
    suggestedActionApi.list.mockResolvedValue([
      {
        id: 1,
        actionType: 'SUGGEST_ISSUE',
        title: 'High temperature detected',
        message: 'Temperature exceeds safe operating range.',
        severity: 'HIGH',
        confidence: 'LOW',
        sourceRuleCodes: 'HIGH_TEMP',
        status: 'PENDING',
        createdAt: 1_700_000_000_000,
      },
    ]);
    suggestedActionApi.getDetail.mockResolvedValue({
      id: 1,
      status: 'PENDING',
      confidence: 'LOW',
      evaluationReportTemplateVersion: 1,
      evaluationReportEvaluatedAt: 1_700_000_000_000,
      evaluationReportStatus: 'SUCCESS',
      explanation: {
        matchedRuleCode: 'HIGH_TEMP',
        conditionDescription: 'HIGH_TEMP > 90',
        actualValue: '95',
        configuredActionDescription: 'Suggest HIGH severity Issue.',
      },
    });

    const user = userEvent.setup();
    render(<DecisionAssistantPanel inspectionId={50} />);

    await waitFor(() => {
      expect(screen.getByTestId('confidence-1')).toHaveTextContent('LOW');
    });

    await user.click(screen.getByRole('button', { name: /Open Decision Assistant/i }));

    await waitFor(() => {
      expect(screen.getByTestId('suggestion-why-panel')).toBeInTheDocument();
    });
    expect(screen.getByText('HIGH_TEMP > 90')).toBeInTheDocument();
    expect(screen.getByTestId('approve-btn-1')).toBeInTheDocument();
    expect(screen.getByTestId('reject-btn-1')).toBeInTheDocument();
    expect(screen.getByTestId('dismiss-btn-1')).toBeInTheDocument();
  });

  it('shows read-only list without decision buttons for non-managers', async () => {
    mockAuth.user.role = 'OPERATIONAL_COORDINATOR';
    suggestedActionApi.list.mockResolvedValue([
      {
        id: 2,
        actionType: 'SUGGEST_ISSUE',
        title: 'High temperature detected',
        confidence: 'LOW',
        sourceRuleCodes: 'HIGH_TEMP',
        status: 'PENDING',
        createdAt: 1_700_000_000_000,
      },
    ]);

    render(<DecisionAssistantPanel inspectionId={60} />);

    await waitFor(() => {
      expect(screen.getByText('Suggested Actions')).toBeInTheDocument();
    });
    expect(screen.queryByTestId('approve-btn-2')).not.toBeInTheDocument();
  });

  it('shows empty state when no suggestions exist', async () => {
    suggestedActionApi.list.mockResolvedValue([]);
    render(<DecisionAssistantPanel inspectionId={70} />);
    await waitFor(() => {
      expect(screen.getByTestId('suggested-actions-empty-70')).toHaveTextContent(
        'No suggested actions were generated.'
      );
    });
  });
});
