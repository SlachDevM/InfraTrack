import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import SuggestedActionsPanel from '../../components/inspections/SuggestedActionsPanel';
import suggestedActionApi from '../../services/suggestedActionApi';

vi.mock('../../services/suggestedActionApi', () => ({
  default: {
    list: vi.fn(),
  },
}));

describe('SuggestedActionsPanel', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('displays suggestions with severity and source rule codes', async () => {
    suggestedActionApi.list.mockResolvedValue([
      {
        id: 1,
        inspectionId: 50,
        reportId: 10,
        actionType: 'SUGGEST_ISSUE',
        title: 'High temperature detected',
        message: 'Temperature exceeds safe operating range.',
        severity: 'HIGH',
        matchedRuleCount: 1,
        sourceRuleCodes: 'HIGH_TEMP',
        status: 'PENDING',
        createdAt: 1_700_000_000_000,
      },
    ]);

    render(<SuggestedActionsPanel inspectionId={50} />);

    await waitFor(() => {
      expect(screen.getByTestId('suggested-actions-panel-50')).toBeInTheDocument();
    });

    expect(screen.getByText('High temperature detected')).toBeInTheDocument();
    expect(screen.getByText('HIGH')).toBeInTheDocument();
    expect(screen.getByText('HIGH_TEMP')).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: /accept/i })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: /reject/i })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: /create issue/i })).not.toBeInTheDocument();
  });

  it('shows empty state when no suggestions exist', async () => {
    suggestedActionApi.list.mockResolvedValue([]);

    render(<SuggestedActionsPanel inspectionId={60} />);

    await waitFor(() => {
      expect(screen.getByTestId('suggested-actions-empty-60')).toHaveTextContent(
        'No suggested actions were generated.'
      );
    });
  });

  it('displays API errors gracefully', async () => {
    suggestedActionApi.list.mockRejectedValue({
      response: { data: { message: 'Forbidden' }, status: 403 },
    });

    render(<SuggestedActionsPanel inspectionId={70} />);

    await waitFor(() => {
      expect(screen.getByTestId('suggested-actions-error-70')).toBeInTheDocument();
    });
  });
});
