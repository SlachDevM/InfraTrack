import { describe, it, expect, vi, afterEach } from 'vitest';
import { render, screen, cleanup } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import PaginationControls from '../../components/PaginationControls';

afterEach(cleanup);

describe('PaginationControls', () => {
  it('disables Previous on the first page', () => {
    render(
      <PaginationControls
        page={0}
        totalPages={3}
        onPrevious={vi.fn()}
        onNext={vi.fn()}
      />
    );

    expect(screen.getByTestId('pagination-previous')).toBeDisabled();
    expect(screen.getByTestId('pagination-next')).toBeEnabled();
    expect(screen.getByText('Page 1 of 3')).toBeInTheDocument();
  });

  it('disables Next on the last page', () => {
    render(
      <PaginationControls
        page={2}
        totalPages={3}
        onPrevious={vi.fn()}
        onNext={vi.fn()}
      />
    );

    expect(screen.getByTestId('pagination-previous')).toBeEnabled();
    expect(screen.getByTestId('pagination-next')).toBeDisabled();
    expect(screen.getByText('Page 3 of 3')).toBeInTheDocument();
  });

  it('calls next and previous handlers', async () => {
    const user = userEvent.setup();
    const onPrevious = vi.fn();
    const onNext = vi.fn();

    render(
      <PaginationControls
        page={1}
        totalPages={3}
        onPrevious={onPrevious}
        onNext={onNext}
      />
    );

    await user.click(screen.getByTestId('pagination-previous'));
    await user.click(screen.getByTestId('pagination-next'));

    expect(onPrevious).toHaveBeenCalledTimes(1);
    expect(onNext).toHaveBeenCalledTimes(1);
  });

  it('disables both buttons while loading', () => {
    render(
      <PaginationControls
        page={1}
        totalPages={3}
        loading
        onPrevious={vi.fn()}
        onNext={vi.fn()}
      />
    );

    expect(screen.getByTestId('pagination-previous')).toBeDisabled();
    expect(screen.getByTestId('pagination-next')).toBeDisabled();
  });
});
