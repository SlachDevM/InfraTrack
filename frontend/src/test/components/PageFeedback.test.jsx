import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import {
  PageErrorMessage,
  PageSuccessMessage,
  ListLoadingIndicator,
  TableEmptyRow,
} from '../../components/PageFeedback';

describe('PageFeedback', () => {
  it('renders error message with alert role', () => {
    render(<PageErrorMessage message="Something failed." />);
    expect(screen.getByRole('alert')).toHaveTextContent('Something failed.');
  });

  it('renders success message with status role', () => {
    render(<PageSuccessMessage message="Saved successfully." />);
    expect(screen.getByRole('status')).toHaveTextContent('Saved successfully.');
  });

  it('renders list loading indicator', () => {
    render(<ListLoadingIndicator />);
    expect(screen.getByText('Loading page...')).toHaveAttribute('role', 'status');
  });

  it('renders table empty row with empty-state styling', () => {
    render(
      <table>
        <tbody>
          <TableEmptyRow colSpan={3} message="No rows found." />
        </tbody>
      </table>
    );

    const cell = screen.getByText('No rows found.');
    expect(cell).toHaveClass('empty-state');
    expect(cell).toHaveAttribute('colspan', '3');
  });
});
